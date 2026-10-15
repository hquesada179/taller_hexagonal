package com.stagepass.live.application.service;

import com.stagepass.live.application.port.out.BoletoRepository;
import com.stagepass.live.application.port.out.CatalogoRepository;
import com.stagepass.live.application.port.out.ClienteRepository;
import com.stagepass.live.application.port.out.OcupacionRepository;
import com.stagepass.live.application.port.out.PagoGateway;
import com.stagepass.live.application.port.out.ReservaRepository;
import com.stagepass.live.domain.enums.EstadoEvento;
import com.stagepass.live.domain.enums.ProveedorPago;
import com.stagepass.live.domain.enums.TipoEvento;
import com.stagepass.live.domain.exception.AsientoNoDisponibleException;
import com.stagepass.live.domain.exception.EventoNoDisponibleException;
import com.stagepass.live.domain.exception.LimiteBoletosExcedidoException;
import com.stagepass.live.domain.exception.RecursoNoEncontradoException;
import com.stagepass.live.domain.model.Asiento;
import com.stagepass.live.domain.model.Cliente;
import com.stagepass.live.domain.model.Evento;
import com.stagepass.live.domain.model.Reserva;
import com.stagepass.live.domain.policy.CalculadoraTarifas;
import com.stagepass.live.domain.policy.PoliticaLimite;
import com.stagepass.live.domain.policy.PoliticaReserva;
import com.stagepass.live.testutil.RelojFijo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompraServiceTest {

    private static final String EVENTO_ID = "evt-1";
    private static final String RECINTO_ID = "recinto-1";
    private static final String CLIENTE_ID = "cliente-1";

    private final CatalogoRepository catalogoRepository = mock(CatalogoRepository.class);
    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final ReservaRepository reservaRepository = mock(ReservaRepository.class);
    private final OcupacionRepository ocupacionRepository = mock(OcupacionRepository.class);
    private final BoletoRepository boletoRepository = mock(BoletoRepository.class);
    private final PagoGateway pagoGateway = mock(PagoGateway.class);
    private final RelojFijo reloj = new RelojFijo(LocalDateTime.of(2026, 10, 15, 10, 0, 0));

    private CompraService compraService;

    @BeforeEach
    void prepararEscenarioBase() {
        compraService = new CompraService(catalogoRepository, clienteRepository, reservaRepository,
                ocupacionRepository, boletoRepository, pagoGateway, reloj,
                new CalculadoraTarifas(), new PoliticaReserva(), new PoliticaLimite());

        when(clienteRepository.buscar(CLIENTE_ID))
                .thenReturn(Optional.of(new Cliente(CLIENTE_ID, "Cliente de Prueba", "c@example.com", "+57 300", "CO")));
        when(catalogoRepository.evento(EVENTO_ID)).thenReturn(Optional.of(
                new Evento(EVENTO_ID, "Evento de prueba", TipoEvento.CONCIERTO, RECINTO_ID,
                        LocalDateTime.of(2026, 12, 1, 20, 0), EstadoEvento.PROGRAMADO)));
        when(catalogoRepository.asientos(EVENTO_ID)).thenReturn(List.of(
                new Asiento("a1", RECINTO_ID, "A1", new BigDecimal("100000")),
                new Asiento("a2", RECINTO_ID, "A2", new BigDecimal("100000"))));
        when(boletoRepository.contarEmitidos(CLIENTE_ID, EVENTO_ID)).thenReturn(0);
        when(reservaRepository.contarReservados(CLIENTE_ID, EVENTO_ID)).thenReturn(0);
        when(ocupacionRepository.reservarAtomico(eq(EVENTO_ID), anyList(), anyString(), any(), eq(CLIENTE_ID)))
                .thenReturn(true);
    }

    @Test
    void iniciaUnaCompraValidaBloqueandoAsientosYRegistrandoPago() {
        Reserva reserva = compraService.iniciar(CLIENTE_ID, EVENTO_ID, List.of("a1", "a2"), ProveedorPago.WOMPI);

        assertThat(reserva.getSubtotal()).isEqualTo("200000.00");
        assertThat(reserva.getTarifaServicio()).isEqualTo("20000.00"); // 1-2 boletos: 10%
        assertThat(reserva.getDescuento()).isEqualTo("0.00");
        assertThat(reserva.getTotal()).isEqualTo("220000.00");
        assertThat(reserva.getExpiraEn()).isEqualTo(LocalDateTime.of(2026, 10, 15, 10, 10, 0));

        verify(ocupacionRepository).reservarAtomico(eq(EVENTO_ID), eq(List.of("a1", "a2")), anyString(), any(), eq(CLIENTE_ID));
        verify(reservaRepository).guardar(any(Reserva.class));
        verify(pagoGateway).registrar(any(Reserva.class), eq(ProveedorPago.WOMPI));
    }

    @Test
    void rechazaCompraSiElEventoNoEstaProgramado() {
        when(catalogoRepository.evento(EVENTO_ID)).thenReturn(Optional.of(
                new Evento(EVENTO_ID, "Evento cancelado", TipoEvento.CONCIERTO, RECINTO_ID,
                        LocalDateTime.of(2026, 12, 1, 20, 0), EstadoEvento.CANCELADO)));

        assertThatThrownBy(() -> compraService.iniciar(CLIENTE_ID, EVENTO_ID, List.of("a1"), ProveedorPago.WOMPI))
                .isInstanceOf(EventoNoDisponibleException.class);
    }

    @Test
    void rechazaCompraSiElClienteNoExiste() {
        when(clienteRepository.buscar("cliente-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compraService.iniciar("cliente-inexistente", EVENTO_ID, List.of("a1"), ProveedorPago.WOMPI))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void rechazaCompraSiElAsientoNoPerteneceAlRecintoDelEvento() {
        assertThatThrownBy(() -> compraService.iniciar(CLIENTE_ID, EVENTO_ID, List.of("asiento-de-otro-recinto"), ProveedorPago.WOMPI))
                .isInstanceOf(AsientoNoDisponibleException.class);
    }

    @Test
    void rechazaCompraQueExcedeElLimiteDeSeisBoletosPorCliente() {
        when(boletoRepository.contarEmitidos(CLIENTE_ID, EVENTO_ID)).thenReturn(5);

        assertThatThrownBy(() -> compraService.iniciar(CLIENTE_ID, EVENTO_ID, List.of("a1", "a2"), ProveedorPago.WOMPI))
                .isInstanceOf(LimiteBoletosExcedidoException.class);
    }

    @Test
    void rechazaCompraSiLosAsientosYaFueronTomadosPorOtraSolicitudConcurrente() {
        when(ocupacionRepository.reservarAtomico(eq(EVENTO_ID), anyList(), anyString(), any(), eq(CLIENTE_ID)))
                .thenReturn(false);

        assertThatThrownBy(() -> compraService.iniciar(CLIENTE_ID, EVENTO_ID, List.of("a1"), ProveedorPago.WOMPI))
                .isInstanceOf(AsientoNoDisponibleException.class);

        verify(reservaRepository, never()).guardar(any());
    }

    @Test
    void liberaLosAsientosSiFallaElGuardadoDeLaReservaDespuesDeBloquearlos() {
        org.mockito.Mockito.doThrow(new RuntimeException("fallo simulado de base de datos"))
                .when(reservaRepository).guardar(any());

        assertThatThrownBy(() -> compraService.iniciar(CLIENTE_ID, EVENTO_ID, List.of("a1"), ProveedorPago.WOMPI))
                .isInstanceOf(RuntimeException.class);

        verify(ocupacionRepository).liberar(anyString());
    }

    @Test
    void rechazaSeleccionarElMismoAsientoDosVecesEnUnaMismaCompra() {
        assertThatThrownBy(() -> compraService.iniciar(CLIENTE_ID, EVENTO_ID, List.of("a1", "a1"), ProveedorPago.WOMPI))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dosComprasSimultaneasDelMismoClienteParaElMismoEventoNoSuperanElLimiteDeSeisBoletos() throws InterruptedException {
        // Regresion: dos compras CONCURRENTES del mismo cliente para el mismo evento,
        // con asientos DISTINTOS (4 + 4 = 8 > 6), no deben poder aprobarse ambas. Antes
        // de la correccion, CompraService leia "reservados" fuera de cualquier candado,
        // por lo que ambos hilos podian leer el conteo antes de que cualquiera guardara
        // su reserva, y las dos pasaban la validacion del limite.
        when(catalogoRepository.asientos(EVENTO_ID)).thenReturn(List.of(
                new Asiento("a1", RECINTO_ID, "A1", new BigDecimal("100000")),
                new Asiento("a2", RECINTO_ID, "A2", new BigDecimal("100000")),
                new Asiento("a3", RECINTO_ID, "A3", new BigDecimal("100000")),
                new Asiento("a4", RECINTO_ID, "A4", new BigDecimal("100000")),
                new Asiento("a5", RECINTO_ID, "A5", new BigDecimal("100000")),
                new Asiento("a6", RECINTO_ID, "A6", new BigDecimal("100000")),
                new Asiento("a7", RECINTO_ID, "A7", new BigDecimal("100000")),
                new Asiento("a8", RECINTO_ID, "A8", new BigDecimal("100000"))));

        java.util.concurrent.atomic.AtomicInteger boletosYaReservados = new java.util.concurrent.atomic.AtomicInteger(0);
        when(reservaRepository.contarReservados(CLIENTE_ID, EVENTO_ID)).thenAnswer(invocation -> {
            int valorLeido = boletosYaReservados.get();
            // Ensancha deliberadamente la ventana entre "leer el conteo" y "guardar la
            // reserva", para que la carrera sea reproducible de forma fiable en la prueba.
            Thread.sleep(80);
            return valorLeido;
        });
        org.mockito.Mockito.doAnswer(invocation -> {
            Reserva reserva = invocation.getArgument(0);
            boletosYaReservados.addAndGet(reserva.cantidadBoletos());
            return null;
        }).when(reservaRepository).guardar(any());

        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        java.util.concurrent.CountDownLatch salida = new java.util.concurrent.CountDownLatch(2);
        java.util.List<Exception> errores = new java.util.concurrent.CopyOnWriteArrayList<>();
        java.util.concurrent.atomic.AtomicInteger exitos = new java.util.concurrent.atomic.AtomicInteger(0);

        java.util.function.Consumer<List<String>> comprarCuatro = asientos -> {
            try {
                compraService.iniciar(CLIENTE_ID, EVENTO_ID, asientos, ProveedorPago.WOMPI);
                exitos.incrementAndGet();
            } catch (LimiteBoletosExcedidoException ex) {
                // Resultado esperado para una de las dos compras concurrentes.
            } catch (Exception ex) {
                errores.add(ex);
            } finally {
                salida.countDown();
            }
        };

        pool.submit(() -> comprarCuatro.accept(List.of("a1", "a2", "a3", "a4")));
        pool.submit(() -> comprarCuatro.accept(List.of("a5", "a6", "a7", "a8")));

        boolean terminaron = salida.await(5, java.util.concurrent.TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(terminaron).isTrue();
        assertThat(errores).isEmpty();
        assertThat(exitos.get()).isEqualTo(1);
    }
}
