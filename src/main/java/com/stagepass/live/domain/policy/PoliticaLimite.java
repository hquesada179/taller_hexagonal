package com.stagepass.live.domain.policy;

import com.stagepass.live.domain.exception.LimiteBoletosExcedidoException;

/**
 * Limite maximo de boletos que un cliente puede acumular para un mismo evento,
 * sumando boletos ya emitidos y reservas activas (vigentes) pendientes de pago,
 * mas los que se solicitan en la nueva compra. El llamador es responsable de no
 * contar dos veces la misma compra (por ejemplo, excluyendo la reserva actual
 * del conteo de "reservados" cuando se re-valida una reserva ya existente).
 */
public class PoliticaLimite {

    public static final int MAX_BOLETOS_POR_DEFECTO = 6;

    private final int maxBoletos;

    public PoliticaLimite() {
        this(MAX_BOLETOS_POR_DEFECTO);
    }

    public PoliticaLimite(int maxBoletos) {
        if (maxBoletos <= 0) {
            throw new IllegalArgumentException("maxBoletos debe ser mayor que cero");
        }
        this.maxBoletos = maxBoletos;
    }

    public void validar(int emitidos, int reservados, int nuevos) {
        int total = emitidos + reservados + nuevos;
        if (total > maxBoletos) {
            throw new LimiteBoletosExcedidoException(
                    "El cliente excederia el limite de " + maxBoletos + " boletos para este evento "
                            + "(emitidos=" + emitidos + ", reservados=" + reservados + ", solicitados=" + nuevos + ")");
        }
    }

    public int getMaxBoletos() {
        return maxBoletos;
    }
}
