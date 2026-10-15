package com.stagepass.live.domain.policy;

import com.stagepass.live.domain.enums.EstadoReserva;
import com.stagepass.live.domain.exception.ReservaNoVigenteException;
import com.stagepass.live.domain.model.Reserva;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class PoliticaReservaTest {

    private final PoliticaReserva politica = new PoliticaReserva();

    @Test
    void elBloqueoDuraExactamenteDiezMinutos() {
        LocalDateTime desde = LocalDateTime.of(2026, 10, 15, 10, 0, 0);
        LocalDateTime expiracion = politica.expiracion(desde);

        assertThat(expiracion).isEqualTo(LocalDateTime.of(2026, 10, 15, 10, 10, 0));
    }

    @Test
    void unaReservaEsVigenteJustoAntesDeVencer() {
        LocalDateTime creada = LocalDateTime.of(2026, 10, 15, 10, 0, 0);
        Reserva reserva = reservaPendiente(creada);

        assertThatCode(() -> politica.validarVigencia(reserva, creada.plusMinutes(9).plusSeconds(59)))
                .doesNotThrowAnyException();
    }

    @Test
    void unaReservaYaNoEsVigenteAlCumplirseLosDiezMinutos() {
        LocalDateTime creada = LocalDateTime.of(2026, 10, 15, 10, 0, 0);
        Reserva reserva = reservaPendiente(creada);

        assertThatThrownBy(() -> politica.validarVigencia(reserva, creada.plusMinutes(10)))
                .isInstanceOf(ReservaNoVigenteException.class);
    }

    private Reserva reservaPendiente(LocalDateTime creada) {
        return new Reserva("r1", "evt1", "cli1", List.of("a1"),
                new BigDecimal("100000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO,
                new BigDecimal("110000.00"), politica.expiracion(creada), EstadoReserva.PENDIENTE_PAGO);
    }
}
