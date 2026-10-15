package com.stagepass.live.application.service;

import com.stagepass.live.application.port.in.ConfirmacionPort;
import com.stagepass.live.application.port.out.BoletoRepository;
import com.stagepass.live.application.port.out.ClienteRepository;
import com.stagepass.live.application.port.out.FacturaPort;
import com.stagepass.live.application.port.out.FirmaPort;
import com.stagepass.live.application.port.out.NotificacionPort;
import com.stagepass.live.application.port.out.OcupacionRepository;
import com.stagepass.live.application.port.out.PagoGateway;
import com.stagepass.live.application.port.out.RelojPort;
import com.stagepass.live.application.port.out.ReservaRepository;
import com.stagepass.live.domain.enums.EstadoReserva;
import com.stagepass.live.domain.exception.RecursoNoEncontradoException;
import com.stagepass.live.domain.exception.ReservaNoVigenteException;
import com.stagepass.live.domain.model.BoletoDigital;
import com.stagepass.live.domain.model.Cliente;
import com.stagepass.live.domain.model.Pago;
import com.stagepass.live.domain.model.Reserva;
import com.stagepass.live.domain.policy.PoliticaEmision;
import com.stagepass.live.domain.vo.ConfirmacionPago;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Caso de uso de confirmacion de pago y emision de boletos.
 *
 * Orden de operaciones disenado deliberadamente para que una reserva nunca
 * quede APROBADA sin sus boletos: los boletos se preparan (firmados) en
 * memoria ANTES de intentar la transicion atomica de estado, y la rama de
 * "reserva ya aprobada" reintenta la emision si los boletos aun no existen
 * (recuperacion ante fallos parciales / webhooks duplicados).
 */
public class ConfirmacionService implements ConfirmacionPort {

    private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGITUD_CODIGO = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ReservaRepository reservaRepository;
    private final OcupacionRepository ocupacionRepository;
    private final BoletoRepository boletoRepository;
    private final PagoGateway pagoGateway;
    private final ClienteRepository clienteRepository;
    private final FirmaPort firmaPort;
    private final NotificacionPort notificacionPort;
    private final FacturaPort facturaPort;
    private final RelojPort relojPort;
    private final PoliticaEmision politicaEmision;

    public ConfirmacionService(ReservaRepository reservaRepository,
                                OcupacionRepository ocupacionRepository,
                                BoletoRepository boletoRepository,
                                PagoGateway pagoGateway,
                                ClienteRepository clienteRepository,
                                FirmaPort firmaPort,
                                NotificacionPort notificacionPort,
                                FacturaPort facturaPort,
                                RelojPort relojPort,
                                PoliticaEmision politicaEmision) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository);
        this.ocupacionRepository = Objects.requireNonNull(ocupacionRepository);
        this.boletoRepository = Objects.requireNonNull(boletoRepository);
        this.pagoGateway = Objects.requireNonNull(pagoGateway);
        this.clienteRepository = Objects.requireNonNull(clienteRepository);
        this.firmaPort = Objects.requireNonNull(firmaPort);
        this.notificacionPort = Objects.requireNonNull(notificacionPort);
        this.facturaPort = Objects.requireNonNull(facturaPort);
        this.relojPort = Objects.requireNonNull(relojPort);
        this.politicaEmision = Objects.requireNonNull(politicaEmision);
    }

    @Override
    public void confirmar(ConfirmacionPago datos) {
        Objects.requireNonNull(datos, "datos es obligatorio");

        Reserva reserva = reservaRepository.buscar(datos.getReservaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada: " + datos.getReservaId()));

        Pago pago = pagoGateway.verificar(datos);

        // Si la reserva YA esta APROBADA, esto es un webhook duplicado (o un reintento tras un
        // fallo parcial anterior). Debe resolverse de forma idempotente ANTES de validar vigencia:
        // una reserva aprobada nunca es "vigente" segun Reserva.estaVigente() (eso solo aplica a
        // PENDIENTE_PAGO), por lo que invocar politicaEmision.validar() aqui la rechazaria con un
        // mensaje de "vencida" incorrecto.
        if (reserva.getEstado() == EstadoReserva.APROBADA) {
            reemitirBoletosSiFaltan(reserva);
            return;
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE_PAGO) {
            throw new ReservaNoVigenteException(
                    "La reserva " + reserva.getId() + " no puede aprobarse (estado actual: " + reserva.getEstado() + ")");
        }

        LocalDateTime instanteConfirmacion = datos.getMomentoConfirmacionProveedor();
        politicaEmision.validar(reserva, pago, instanteConfirmacion);

        List<BoletoDigital> boletosPreparados = prepararBoletosFirmados(reserva);

        boolean aprobadaAhora = reservaRepository.aprobarSiVigente(reserva.getId(), instanteConfirmacion);

        if (!aprobadaAhora) {
            Reserva actual = reservaRepository.buscar(reserva.getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada: " + reserva.getId()));
            if (actual.getEstado() != EstadoReserva.APROBADA) {
                throw new ReservaNoVigenteException(
                        "La reserva " + reserva.getId() + " no puede aprobarse (estado actual: " + actual.getEstado() + ")");
            }
            reemitirBoletosSiFaltan(actual);
            return;
        }

        emitirBoletosYNotificar(reserva, boletosPreparados);
    }

    private void reemitirBoletosSiFaltan(Reserva reservaAprobada) {
        List<BoletoDigital> existentes = boletoRepository.porReserva(reservaAprobada.getId());
        if (existentes.isEmpty()) {
            emitirBoletosYNotificar(reservaAprobada, prepararBoletosFirmados(reservaAprobada));
        }
        // Si ya existen boletos, la confirmacion es idempotente: no se hace nada mas.
    }

    private void emitirBoletosYNotificar(Reserva reserva, List<BoletoDigital> boletos) {
        ocupacionRepository.confirmarVenta(reserva.getId());
        boletoRepository.guardarSinDuplicar(boletos);

        Optional<Cliente> cliente = clienteRepository.buscar(reserva.getClienteId());
        cliente.ifPresent(c -> {
            try {
                notificacionPort.enviar(c, boletos);
            } catch (RuntimeException ex) {
                // La notificacion es "best effort": no debe revertir una emision ya persistida.
            }
            try {
                facturaPort.emitir(reserva, c);
            } catch (RuntimeException ex) {
                // La facturacion tampoco es critica para la invariante "aprobada implica boletos".
            }
        });
    }

    private List<BoletoDigital> prepararBoletosFirmados(Reserva reserva) {
        LocalDateTime fechaEmision = relojPort.ahora();
        return reserva.getAsientoIds().stream()
                .map(asientoId -> {
                    BoletoDigital boleto = BoletoDigital.sinFirma(
                            java.util.UUID.randomUUID().toString(),
                            reserva.getId(),
                            asientoId,
                            generarCodigoRedencion(),
                            fechaEmision);
                    String firma = firmaPort.firmar(boleto.datosParaFirma(), reserva.getEventoId());
                    boleto.firmar(firma);
                    return boleto;
                })
                .toList();
    }

    private String generarCodigoRedencion() {
        StringBuilder sb = new StringBuilder(LONGITUD_CODIGO);
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            sb.append(ALFABETO_CODIGO.charAt(RANDOM.nextInt(ALFABETO_CODIGO.length())));
        }
        return sb.toString();
    }

    @Override
    public Optional<Pago> consultarPago(String reservaId) {
        return pagoGateway.consultar(reservaId);
    }
}
