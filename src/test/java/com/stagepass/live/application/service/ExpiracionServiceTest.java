package com.stagepass.live.application.service;

import com.stagepass.live.application.port.in.ResultadoExpiracion;
import com.stagepass.live.application.port.out.OcupacionRepository;
import com.stagepass.live.application.port.out.ReservaRepository;
import com.stagepass.live.domain.enums.EstadoReserva;
import com.stagepass.live.domain.model.Reserva;
import com.stagepass.live.testutil.RelojFijo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpiracionServiceTest {

    private final ReservaRepository reservaRepository = mock(ReservaRepository.class);
    private final OcupacionRepository ocupacionRepository = mock(OcupacionRepository.class);
    private final RelojFijo reloj = new RelojFijo(LocalDateTime.of(2026, 10, 15, 10, 15, 0));

    private final ExpiracionService expiracionService =
            new ExpiracionService(reservaRepository, ocupacionRepository, reloj);

    @Test
    void liberaYExpiraSoloLasReservasQueRealmenteSiguenPendientes() {
        Reserva vencidaUno = reserva("r1");
        Reserva vencidaDos = reserva("r2");
        when(reservaRepository.vencidas(reloj.ahora())).thenReturn(List.of(vencidaUno, vencidaDos));
        when(reservaRepository.expirarSiPendiente("r1", reloj.ahora())).thenReturn(true);
        when(reservaRepository.expirarSiPendiente("r2", reloj.ahora())).thenReturn(false);

        int expiradas = expiracionService.expirarVencidas();

        assertThat(expiradas).isEqualTo(1);
        verify(ocupacionRepository).liberar("r1");
        verify(ocupacionRepository, never()).liberar("r2");
    }

    @Test
    void noHaceNadaCuandoNoHayReservasVencidas() {
        when(reservaRepository.vencidas(reloj.ahora())).thenReturn(List.of());

        assertThat(expiracionService.expirarVencidas()).isZero();
        verify(ocupacionRepository, never()).liberar(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void recuerdaElResultadoDeLaUltimaEjecucion() {
        when(reservaRepository.vencidas(reloj.ahora())).thenReturn(List.of(reserva("r1")));
        when(reservaRepository.expirarSiPendiente("r1", reloj.ahora())).thenReturn(true);

        expiracionService.expirarVencidas();
        ResultadoExpiracion resultado = expiracionService.consultarUltimaEjecucion();

        assertThat(resultado.reservasExpiradas()).isEqualTo(1);
        assertThat(resultado.ejecutadoEn()).isEqualTo(reloj.ahora());
    }

    private Reserva reserva(String id) {
        return new Reserva(id, "evt1", "cli1", List.of("a1"),
                new BigDecimal("100000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO,
                new BigDecimal("110000.00"), LocalDateTime.of(2026, 10, 15, 10, 0, 0), EstadoReserva.PENDIENTE_PAGO);
    }
}
