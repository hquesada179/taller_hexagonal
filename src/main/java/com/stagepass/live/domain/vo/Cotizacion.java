package com.stagepass.live.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Objeto de valor inmutable con el resultado del calculo de tarifas de una compra.
 * Convencion adoptada por el equipo (no especificada explicitamente por el enunciado):
 * TOTAL = SUBTOTAL + TARIFA_ADMINISTRATIVA - DESCUENTO, donde ambos porcentajes
 * se aplican sobre el SUBTOTAL (suma de los precios base de los asientos), nunca
 * en cascada uno sobre el otro.
 */
public final class Cotizacion {

    private final BigDecimal subtotal;
    private final BigDecimal tarifa;
    private final BigDecimal descuento;
    private final BigDecimal total;

    public Cotizacion(BigDecimal subtotal, BigDecimal tarifa, BigDecimal descuento, BigDecimal total) {
        this.subtotal = Objects.requireNonNull(subtotal);
        this.tarifa = Objects.requireNonNull(tarifa);
        this.descuento = Objects.requireNonNull(descuento);
        this.total = Objects.requireNonNull(total);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTarifa() {
        return tarifa;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public BigDecimal getTotal() {
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cotizacion that)) return false;
        return subtotal.equals(that.subtotal) && tarifa.equals(that.tarifa)
                && descuento.equals(that.descuento) && total.equals(that.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subtotal, tarifa, descuento, total);
    }

    @Override
    public String toString() {
        return "Cotizacion{subtotal=%s, tarifa=%s, descuento=%s, total=%s}"
                .formatted(subtotal, tarifa, descuento, total);
    }
}
