package com.stagepass.live.domain.policy;

import com.stagepass.live.domain.exception.ReservaNoVigenteException;
import com.stagepass.live.domain.model.Reserva;

import java.time.LocalDateTime;

/**
 * Centraliza la duracion del bloqueo temporal de asientos y la validacion
 * de vigencia de una reserva. La duracion es configurable (inyectada desde
 * la capa de aplicacion con el valor por defecto exigido por el taller: 10
 * minutos exactos), pero esta clase no depende de ningun framework.
 */
public class PoliticaReserva {

    public static final int DURACION_MINUTOS_POR_DEFECTO = 10;

    private final int duracionMinutos;

    public PoliticaReserva() {
        this(DURACION_MINUTOS_POR_DEFECTO);
    }

    public PoliticaReserva(int duracionMinutos) {
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("duracionMinutos debe ser mayor que cero");
        }
        this.duracionMinutos = duracionMinutos;
    }

    public LocalDateTime expiracion(LocalDateTime desde) {
        return desde.plusMinutes(duracionMinutos);
    }

    /**
     * Valida que la reserva este vigente en el instante indicado. El instante
     * NO debe ser necesariamente "el ahora del servidor": para la confirmacion
     * de pago, el llamador debe pasar el instante real en que el PROVEEDOR de
     * pago confirmo la transaccion, nunca la hora de recepcion del webhook.
     */
    public void validarVigencia(Reserva reserva, LocalDateTime instante) {
        if (!reserva.estaVigente(instante)) {
            throw new ReservaNoVigenteException(
                    "La reserva " + reserva.getId() + " no esta vigente en el instante " + instante);
        }
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }
}
