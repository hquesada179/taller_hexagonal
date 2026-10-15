package com.stagepass.live.domain.policy;

import com.stagepass.live.domain.exception.PagoInvalidoException;
import com.stagepass.live.domain.exception.ReservaNoVigenteException;
import com.stagepass.live.domain.model.Pago;
import com.stagepass.live.domain.model.Reserva;

import java.time.LocalDateTime;

/**
 * Reglas que deben cumplirse antes de emitir boletos:
 *  - El pago debe estar verificado como aprobado.
 *  - El pago debe corresponder exactamente a la reserva (mismo id).
 *  - El monto pagado debe coincidir con el total de la reserva.
 *  - La reserva debe estar vigente en el instante en que el PROVEEDOR de pago
 *    confirmo la transaccion (parametro "instanteConfirmacion"), no en el
 *    instante en que el servidor recibio el webhook.
 */
public class PoliticaEmision {

    public void validar(Reserva reserva, Pago pago, LocalDateTime instanteConfirmacion) {
        if (pago == null || !pago.estaVerificado()) {
            throw new PagoInvalidoException("El pago de la reserva " + reserva.getId() + " no esta verificado");
        }
        if (!pago.getReservaId().equals(reserva.getId())) {
            throw new PagoInvalidoException(
                    "El pago " + pago.getId() + " no corresponde a la reserva " + reserva.getId());
        }
        if (pago.getMonto().compareTo(reserva.getTotal()) != 0) {
            throw new PagoInvalidoException(
                    "El monto pagado (" + pago.getMonto() + ") no coincide con el total de la reserva ("
                            + reserva.getTotal() + ")");
        }
        if (!reserva.estaVigente(instanteConfirmacion)) {
            throw new ReservaNoVigenteException(
                    "La reserva " + reserva.getId() + " ya habia vencido cuando el proveedor confirmo el pago ("
                            + instanteConfirmacion + ")");
        }
    }
}
