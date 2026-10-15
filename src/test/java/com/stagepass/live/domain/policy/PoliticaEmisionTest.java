package com.stagepass.live.domain.policy;

import com.stagepass.live.domain.enums.EstadoPago;
import com.stagepass.live.domain.enums.EstadoReserva;
import com.stagepass.live.domain.enums.ProveedorPago;
import com.stagepass.live.domain.exception.PagoInvalidoException;
import com.stagepass.live.domain.exception.ReservaNoVigenteException;
import com.stagepass.live.domain.model.Pago;
import com.stagepass.live.domain.model.Reserva;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoliticaEmisionTest {

    private final PoliticaEmision politica = new PoliticaEmision();
    private final LocalDateTime creada = LocalDateTime.of(2026, 10, 15, 10, 0, 0);
    private final LocalDateTime expira = creada.plusMinutes(10);

    @Test
    void permiteEmitirConPagoAprobadoDentroDeLaVigencia() {
        Reserva reserva = reserva();
        Pago pago = pagoAprobado(reserva);

        assertThatCode(() -> politica.validar(reserva, pago, creada.plusMinutes(5)))
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaSiElPagoNoEstaVerificado() {
        Reserva reserva = reserva();
        Pago pago = new Pago("p1", reserva.getId(), "ref-1", reserva.getTotal(), EstadoPago.PENDIENTE, ProveedorPago.WOMPI);

        assertThatThrownBy(() -> politica.validar(reserva, pago, creada.plusMinutes(5)))
                .isInstanceOf(PagoInvalidoException.class);
    }

    @Test
    void rechazaSiElMontoNoCoincideConElTotalDeLaReserva() {
        Reserva reserva = reserva();
        Pago pago = new Pago("p1", reserva.getId(), "ref-1", new BigDecimal("1.00"), EstadoPago.APROBADO, ProveedorPago.WOMPI);

        assertThatThrownBy(() -> politica.validar(reserva, pago, creada.plusMinutes(5)))
                .isInstanceOf(PagoInvalidoException.class);
    }

    @Test
    void rechazaConfirmacionTardiaAunqueElPagoSeaValido_segunElInstanteDelProveedor() {
        Reserva reserva = reserva();
        Pago pago = pagoAprobado(reserva);

        // El proveedor confirma DESPUES de que el bloqueo de 10 minutos ya vencio.
        assertThatThrownBy(() -> politica.validar(reserva, pago, expira.plusSeconds(1)))
                .isInstanceOf(ReservaNoVigenteException.class);
    }

    private Reserva reserva() {
        return new Reserva("r1", "evt1", "cli1", List.of("a1"),
                new BigDecimal("100000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO,
                new BigDecimal("110000.00"), expira, EstadoReserva.PENDIENTE_PAGO);
    }

    private Pago pagoAprobado(Reserva reserva) {
        return new Pago("p1", reserva.getId(), "ref-1", reserva.getTotal(), EstadoPago.APROBADO, ProveedorPago.WOMPI);
    }
}
