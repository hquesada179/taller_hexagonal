package com.stagepass.live.application.service;

import com.stagepass.live.application.port.in.CompraPort;
import com.stagepass.live.application.port.out.BoletoRepository;
import com.stagepass.live.application.port.out.CatalogoRepository;
import com.stagepass.live.application.port.out.ClienteRepository;
import com.stagepass.live.application.port.out.OcupacionRepository;
import com.stagepass.live.application.port.out.PagoGateway;
import com.stagepass.live.application.port.out.RelojPort;
import com.stagepass.live.application.port.out.ReservaRepository;
import com.stagepass.live.domain.enums.ProveedorPago;
import com.stagepass.live.domain.exception.AsientoNoDisponibleException;
import com.stagepass.live.domain.exception.EventoNoDisponibleException;
import com.stagepass.live.domain.exception.RecursoNoEncontradoException;
import com.stagepass.live.domain.model.Asiento;
import com.stagepass.live.domain.model.Evento;
import com.stagepass.live.domain.model.Reserva;
import com.stagepass.live.domain.policy.CalculadoraTarifas;
import com.stagepass.live.domain.policy.PoliticaLimite;
import com.stagepass.live.domain.policy.PoliticaReserva;
import com.stagepass.live.domain.vo.Cotizacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso de inicio de compra: valida evento, asientos y limite de
 * boletos, bloquea atomicamente los asientos por 10 minutos, calcula la
 * cotizacion y registra la reserva pendiente de pago.
 *
 * LIMITACION TECNICA CONOCIDA (documentada tambien en el README): la
 * verificacion del limite de 6 boletos por cliente/evento se serializa con
 * un candado en memoria por (clienteId, eventoId) para cerrar la ventana de
 * carrera entre "contar" y "guardar la reserva". Esto evita que dos compras
 * simultaneas del mismo cliente para el mismo evento, con asientos
 * distintos, superen el limite acumulado. Es correcto para una UNICA
 * instancia de la aplicacion (el alcance de este taller); en un despliegue
 * con varias instancias se necesitaria un candado distribuido o un contador
 * atomico en la base de datos.
 */
public class CompraService implements CompraPort {

    private final ConcurrentHashMap<String, Object> candadosPorClienteEvento = new ConcurrentHashMap<>();

    private final CatalogoRepository catalogoRepository;
    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;
    private final OcupacionRepository ocupacionRepository;
    private final BoletoRepository boletoRepository;
    private final PagoGateway pagoGateway;
    private final RelojPort relojPort;
    private final CalculadoraTarifas calculadoraTarifas;
    private final PoliticaReserva politicaReserva;
    private final PoliticaLimite politicaLimite;

    public CompraService(CatalogoRepository catalogoRepository,
                          ClienteRepository clienteRepository,
                          ReservaRepository reservaRepository,
                          OcupacionRepository ocupacionRepository,
                          BoletoRepository boletoRepository,
                          PagoGateway pagoGateway,
                          RelojPort relojPort,
                          CalculadoraTarifas calculadoraTarifas,
                          PoliticaReserva politicaReserva,
                          PoliticaLimite politicaLimite) {
        this.catalogoRepository = Objects.requireNonNull(catalogoRepository);
        this.clienteRepository = Objects.requireNonNull(clienteRepository);
        this.reservaRepository = Objects.requireNonNull(reservaRepository);
        this.ocupacionRepository = Objects.requireNonNull(ocupacionRepository);
        this.boletoRepository = Objects.requireNonNull(boletoRepository);
        this.pagoGateway = Objects.requireNonNull(pagoGateway);
        this.relojPort = Objects.requireNonNull(relojPort);
        this.calculadoraTarifas = Objects.requireNonNull(calculadoraTarifas);
        this.politicaReserva = Objects.requireNonNull(politicaReserva);
        this.politicaLimite = Objects.requireNonNull(politicaLimite);
    }

    @Override
    public Reserva iniciar(String clienteId, String eventoId, List<String> asientoIds, ProveedorPago proveedor) {
        Objects.requireNonNull(clienteId, "clienteId es obligatorio");
        Objects.requireNonNull(eventoId, "eventoId es obligatorio");
        Objects.requireNonNull(proveedor, "proveedor es obligatorio");
        if (asientoIds == null || asientoIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un asiento");
        }
        if (asientoIds.size() != asientoIds.stream().distinct().count()) {
            throw new IllegalArgumentException("No se puede seleccionar el mismo asiento dos veces en una compra");
        }

        clienteRepository.buscar(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + clienteId));

        Evento evento = catalogoRepository.evento(eventoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado: " + eventoId));
        if (!evento.estaProgramado()) {
            throw new EventoNoDisponibleException(
                    "El evento " + eventoId + " no esta programado (estado actual: " + evento.getEstado() + ")");
        }

        Map<String, Asiento> asientosDelRecinto = catalogoRepository.asientos(eventoId).stream()
                .collect(Collectors.toMap(Asiento::getId, Function.identity()));

        List<Asiento> seleccion = asientoIds.stream()
                .map(id -> {
                    Asiento asiento = asientosDelRecinto.get(id);
                    if (asiento == null) {
                        throw new AsientoNoDisponibleException(
                                "El asiento " + id + " no pertenece al recinto del evento " + eventoId);
                    }
                    return asiento;
                })
                .toList();

        int cantidad = seleccion.size();

        // La validacion del limite y la reserva de los asientos se serializan por
        // (clienteId, eventoId): sin este candado, dos compras concurrentes del mismo
        // cliente para el mismo evento (con asientos distintos) podrian leer el mismo
        // conteo de "reservados" antes de que cualquiera de las dos guarde su reserva,
        // y ambas pasarian la validacion aunque juntas superen el limite de boletos.
        Object candado = candadosPorClienteEvento.computeIfAbsent(clienteId + "::" + eventoId, k -> new Object());
        synchronized (candado) {
            int emitidos = boletoRepository.contarEmitidos(clienteId, eventoId);
            int reservados = reservaRepository.contarReservados(clienteId, eventoId);
            politicaLimite.validar(emitidos, reservados, cantidad);

            LocalDateTime ahora = relojPort.ahora();
            LocalDateTime expiraEn = politicaReserva.expiracion(ahora);
            String reservaId = UUID.randomUUID().toString();

            boolean bloqueado = ocupacionRepository.reservarAtomico(eventoId, asientoIds, reservaId, expiraEn, clienteId);
            if (!bloqueado) {
                throw new AsientoNoDisponibleException(
                        "Uno o mas de los asientos seleccionados ya no estan disponibles para este evento");
            }

            try {
                List<BigDecimal> precios = seleccion.stream().map(Asiento::getPrecioBase).toList();
                Cotizacion cotizacion = calculadoraTarifas.calcular(precios);

                Reserva reserva = new Reserva(reservaId, eventoId, clienteId, asientoIds,
                        cotizacion.getSubtotal(), cotizacion.getTarifa(), cotizacion.getDescuento(), cotizacion.getTotal(),
                        expiraEn, com.stagepass.live.domain.enums.EstadoReserva.PENDIENTE_PAGO);

                reservaRepository.guardar(reserva);
                pagoGateway.registrar(reserva, proveedor);

                return reserva;
            } catch (RuntimeException ex) {
                ocupacionRepository.liberar(reservaId);
                throw ex;
            }
        }
    }

    @Override
    public Reserva consultar(String reservaId) {
        return reservaRepository.buscar(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada: " + reservaId));
    }
}
