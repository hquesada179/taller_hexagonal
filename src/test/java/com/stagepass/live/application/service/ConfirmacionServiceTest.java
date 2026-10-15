package com.stagepass.live.application.service;

import com.stagepass.live.application.port.out.BoletoRepository;
import com.stagepass.live.application.port.out.ClienteRepository;
import com.stagepass.live.application.port.out.FacturaPort;
import com.stagepass.live.application.port.out.FirmaPort;
import com.stagepass.live.application.port.out.NotificacionPort;
import com.stagepass.live.application.port.out.OcupacionRepository;
import com.stagepass.live.application.port.out.PagoGateway;
import com.stagepass.live.application.port.out.ReservaRepository;
import com.stagepass.live.domain.enums.EstadoPago;
import com.stagepass.live.domain.enums.EstadoReserva;
import com.stagepass.live.domain.enums.ProveedorPago;
import com.stagepass.live.domain.exception.ReservaNoVigenteException;
import com.stagepass.live.domain.model.BoletoDigital;
import com.stagepass.live.domain.model.Cliente;
import com.stagepass.live.domain.model.Pago;
import com.stagepass.live.domain.model.Reserva;
import com.stagepass.live.domain.policy.PoliticaEmision;
import com.stagepass.live.domain.vo.ConfirmacionPago;
import com.stagepass.live.testutil.RelojFijo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfirmacionServiceTest {

    private static final String RESERVA_ID = "r1";
    private static final LocalDateTime CREADA = LocalDateTime.of(2026, 10, 15, 10, 0, 0);
    private static final LocalDateTime EXPIRA = CREADA.plusMinutes(10);

    private final ReservaRepository reservaRepository = mock(ReservaRepository.class);
    private final OcupacionRepository ocupacionRepository = mock(OcupacionRepository.class);
    private final BoletoRepository boletoRepository = mock(BoletoRepository.class);
    private final PagoGateway pagoGateway = mock(PagoGateway.class);
    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final FirmaPort firmaPort = mock(FirmaPort.class);
    private final NotificacionPort notificacionPort = mock(NotificacionPort.class);
    private final FacturaPort facturaPort = mock(FacturaPort.class);
    private final RelojFijo reloj = new RelojFijo(CREADA.plusMinutes(5));

    private ConfirmacionService confirmacionService;

    @BeforeEach
    void prepararEscenarioBase() {
        confirmacionService = new ConfirmacionService(reservaRepository, ocupacionRepository, boletoRepository,
                pagoGateway, clienteRepository, firmaPort, notificacionPort, facturaPort, reloj, new PoliticaEmision());

        when(reservaRepository.buscar(RESERVA_ID)).thenReturn(Optional.of(reservaPendiente()));
        when(clienteRepository.buscar("cli1")).thenReturn(
                Optional.of(new Cliente("cli1", "Cliente de Prueba", "c@example.com", "+57 300", "CO")));
        when(firmaPort.firmar(anyString(), anyString())).thenReturn("firma-simulada");
    }

    private Reserva reservaPendiente() {
        return new Reserva(RESERVA_ID, "evt1", "cli1", List.of("a1", "a2"),
                new BigDecimal("200000.00"), new BigDecimal("20000.00"), BigDecimal.ZERO,
                new BigDecimal("220000.00"), EXPIRA, EstadoReserva.PENDIENTE_PAGO);
    }

    private Pago pagoAprobado() {
        return new Pago("p1", RESERVA_ID, "ref-1", new BigDecimal("220000.00"), EstadoPago.APROBADO, ProveedorPago.WOMPI);
    }

    private ConfirmacionPago confirmacionExitosa(LocalDateTime instante) {
        return new ConfirmacionPago(RESERVA_ID, "ref-1", ProveedorPago.WOMPI, new BigDecimal("220000.00"), true, instante);
    }

    @Test
    void confirmaElPagoYEmiteUnBoletoPorAsiento() {
        when(pagoGateway.verificar(any())).thenReturn(pagoAprobado());
        when(reservaRepository.aprobarSiVigente(RESERVA_ID, CREADA.plusMinutes(5))).thenReturn(true);

        confirmacionService.confirmar(confirmacionExitosa(CREADA.plusMinutes(5)));

        verify(ocupacionRepository).confirmarVenta(RESERVA_ID);
        verify(boletoRepository).guardarSinDuplicar(argThatListaDeDos());
        verify(notificacionPort).enviar(any(Cliente.class), anyList());
        verify(facturaPort).emitir(any(Reserva.class), any(Cliente.class));
    }

    @Test
    void rechazaConfirmacionCuandoElProveedorConfirmaDespuesDeVencidoElBloqueo() {
        when(pagoGateway.verificar(any())).thenReturn(pagoAprobado());

        assertThatThrownBy(() -> confirmacionService.confirmar(confirmacionExitosa(EXPIRA.plusSeconds(1))))
                .isInstanceOf(ReservaNoVigenteException.class);

        verify(reservaRepository, never()).aprobarSiVigente(anyString(), any());
        verify(boletoRepository, never()).guardarSinDuplicar(anyList());
    }

    @Test
    void esIdempotenteSiElWebhookLlegaMuchoDespuesCuandoLaReservaYaEstaAprobadaDesdeElPrimerFetch() {
        // Reproduce el escenario detectado en pruebas manuales: un webhook duplicado llega cuando
        // la reserva YA esta APROBADA (no solo durante la carrera de aprobarSiVigente). No debe
        // evaluarse la vigencia (la reserva aprobada nunca es "vigente") ni fallar por eso.
        Reserva yaAprobada = reservaPendiente();
        yaAprobada.aprobar();
        when(reservaRepository.buscar(RESERVA_ID)).thenReturn(Optional.of(yaAprobada));
        when(pagoGateway.verificar(any())).thenReturn(pagoAprobado());
        when(boletoRepository.porReserva(RESERVA_ID)).thenReturn(List.of(
                BoletoDigital.sinFirma("b1", RESERVA_ID, "a1", "CODIGO1", CREADA),
                BoletoDigital.sinFirma("b2", RESERVA_ID, "a2", "CODIGO2", CREADA)));

        assertThatCode(() -> confirmacionService.confirmar(confirmacionExitosa(EXPIRA.plusSeconds(30))))
                .doesNotThrowAnyException();

        verify(reservaRepository, never()).aprobarSiVigente(anyString(), any());
        verify(boletoRepository, never()).guardarSinDuplicar(anyList());
        verify(ocupacionRepository, never()).confirmarVenta(anyString());
    }

    @Test
    void esIdempotenteSiElWebhookLlegaDuplicadoYLosBoletosYaFueronEmitidos() {
        when(pagoGateway.verificar(any())).thenReturn(pagoAprobado());
        when(reservaRepository.aprobarSiVigente(RESERVA_ID, CREADA.plusMinutes(5))).thenReturn(false);

        Reserva yaAprobada = new Reserva(RESERVA_ID, "evt1", "cli1", List.of("a1", "a2"),
                new BigDecimal("200000.00"), new BigDecimal("20000.00"), BigDecimal.ZERO,
                new BigDecimal("220000.00"), EXPIRA, EstadoReserva.PENDIENTE_PAGO);
        yaAprobada.aprobar();
        when(reservaRepository.buscar(RESERVA_ID)).thenReturn(Optional.of(reservaPendiente())).thenReturn(Optional.of(yaAprobada));
        when(boletoRepository.porReserva(RESERVA_ID)).thenReturn(List.of(
                BoletoDigital.sinFirma("b1", RESERVA_ID, "a1", "CODIGO1", CREADA)));

        assertThatCode(() -> confirmacionService.confirmar(confirmacionExitosa(CREADA.plusMinutes(5))))
                .doesNotThrowAnyException();

        verify(boletoRepository, never()).guardarSinDuplicar(anyList());
        verify(ocupacionRepository, never()).confirmarVenta(anyString());
    }

    @Test
    void reintentaLaEmisionDeBoletosSiLaReservaQuedoAprobadaPeroSinBoletosPorUnFalloParcial() {
        when(pagoGateway.verificar(any())).thenReturn(pagoAprobado());
        when(reservaRepository.aprobarSiVigente(RESERVA_ID, CREADA.plusMinutes(5))).thenReturn(false);

        Reserva yaAprobada = reservaPendiente();
        yaAprobada.aprobar();
        when(reservaRepository.buscar(RESERVA_ID)).thenReturn(Optional.of(reservaPendiente())).thenReturn(Optional.of(yaAprobada));
        when(boletoRepository.porReserva(RESERVA_ID)).thenReturn(List.of());

        confirmacionService.confirmar(confirmacionExitosa(CREADA.plusMinutes(5)));

        verify(boletoRepository).guardarSinDuplicar(argThatListaDeDos());
    }

    private List<BoletoDigital> argThatListaDeDos() {
        return org.mockito.ArgumentMatchers.argThat(lista -> lista != null && lista.size() == 2);
    }
}
