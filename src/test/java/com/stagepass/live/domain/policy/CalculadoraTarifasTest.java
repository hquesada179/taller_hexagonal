package com.stagepass.live.domain.policy;

import com.stagepass.live.domain.vo.Cotizacion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraTarifasTest {

    private final CalculadoraTarifas calculadora = new CalculadoraTarifas();

    @Test
    void aplicaTarifaDiezPorCientoYSinDescuentoParaUnoADosBoletos() {
        Cotizacion cotizacion = calculadora.calcular(List.of(new BigDecimal("100000"), new BigDecimal("100000")));

        assertThat(cotizacion.getSubtotal()).isEqualTo("200000.00");
        assertThat(cotizacion.getTarifa()).isEqualTo("20000.00");
        assertThat(cotizacion.getDescuento()).isEqualTo("0.00");
        assertThat(cotizacion.getTotal()).isEqualTo("220000.00");
    }

    @Test
    void aplicaTarifaOchoPorCientoYDescuentoCincoPorCientoParaTresACincoBoletos() {
        List<BigDecimal> precios = List.of(
                new BigDecimal("100000"), new BigDecimal("100000"), new BigDecimal("100000"));

        Cotizacion cotizacion = calculadora.calcular(precios);

        assertThat(cotizacion.getSubtotal()).isEqualTo("300000.00");
        assertThat(cotizacion.getTarifa()).isEqualTo("24000.00");
        assertThat(cotizacion.getDescuento()).isEqualTo("15000.00");
        assertThat(cotizacion.getTotal()).isEqualTo("309000.00");
    }

    @Test
    void aplicaTarifaCincoPorCientoYDescuentoDiezPorCientoParaMasDeCincoBoletos() {
        List<BigDecimal> precios = List.of(
                new BigDecimal("100000"), new BigDecimal("100000"), new BigDecimal("100000"),
                new BigDecimal("100000"), new BigDecimal("100000"), new BigDecimal("100000"));

        Cotizacion cotizacion = calculadora.calcular(precios);

        assertThat(cotizacion.getSubtotal()).isEqualTo("600000.00");
        assertThat(cotizacion.getTarifa()).isEqualTo("30000.00");
        assertThat(cotizacion.getDescuento()).isEqualTo("60000.00");
        assertThat(cotizacion.getTotal()).isEqualTo("570000.00");
    }

    @Test
    void rechazaCotizarSinPrecios() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcular(List.of()));
    }
}
