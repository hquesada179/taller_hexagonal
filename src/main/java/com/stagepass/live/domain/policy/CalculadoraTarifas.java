package com.stagepass.live.domain.policy;

import com.stagepass.live.domain.vo.Cotizacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Calcula la cotizacion de una compra segun la cantidad de boletos.
 *
 * Reglas obligatorias del taller:
 *  - 1 a 2 boletos:  tarifa administrativa 10%, descuento 0%.
 *  - 3 a 5 boletos:  tarifa administrativa 8%,  descuento comercial 5%.
 *  - 6 o mas boletos (compra corporativa): tarifa 5%, descuento 10%.
 *
 * Convencion adoptada para el orden de calculo (no especificado explicitamente
 * por el enunciado): ambos porcentajes se aplican sobre el SUBTOTAL (suma de
 * los precios base), de forma independiente, y el total es:
 *   TOTAL = SUBTOTAL + TARIFA_ADMINISTRATIVA - DESCUENTO
 */
public class CalculadoraTarifas {

    private static final BigDecimal TARIFA_1_A_2 = new BigDecimal("0.10");
    private static final BigDecimal DESCUENTO_1_A_2 = BigDecimal.ZERO;

    private static final BigDecimal TARIFA_3_A_5 = new BigDecimal("0.08");
    private static final BigDecimal DESCUENTO_3_A_5 = new BigDecimal("0.05");

    private static final BigDecimal TARIFA_CORPORATIVA = new BigDecimal("0.05");
    private static final BigDecimal DESCUENTO_CORPORATIVA = new BigDecimal("0.10");

    public Cotizacion calcular(List<BigDecimal> precios) {
        Objects.requireNonNull(precios, "precios es obligatorio");
        if (precios.isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos un precio para cotizar");
        }

        BigDecimal subtotal = precios.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        int cantidad = precios.size();
        BigDecimal porcentajeTarifa;
        BigDecimal porcentajeDescuento;

        if (cantidad <= 2) {
            porcentajeTarifa = TARIFA_1_A_2;
            porcentajeDescuento = DESCUENTO_1_A_2;
        } else if (cantidad <= 5) {
            porcentajeTarifa = TARIFA_3_A_5;
            porcentajeDescuento = DESCUENTO_3_A_5;
        } else {
            porcentajeTarifa = TARIFA_CORPORATIVA;
            porcentajeDescuento = DESCUENTO_CORPORATIVA;
        }

        BigDecimal tarifa = subtotal.multiply(porcentajeTarifa).setScale(2, RoundingMode.HALF_UP);
        BigDecimal descuento = subtotal.multiply(porcentajeDescuento).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tarifa).subtract(descuento).setScale(2, RoundingMode.HALF_UP);

        return new Cotizacion(subtotal, tarifa, descuento, total);
    }
}
