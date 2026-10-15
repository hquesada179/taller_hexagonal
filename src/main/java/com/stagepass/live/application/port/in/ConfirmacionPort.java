package com.stagepass.live.application.port.in;

import com.stagepass.live.domain.model.Pago;
import com.stagepass.live.domain.vo.ConfirmacionPago;

import java.util.Optional;

/**
 * Puerto de entrada para confirmar un pago (webhook de la pasarela) y emitir
 * los boletos correspondientes.
 *
 * "consultarPago" es una extension respecto del UML original (que solo
 * definia confirmar); se agrega para poder exponer por API la referencia
 * externa y el estado de un pago, necesarios para demostrar el flujo de
 * pago simulado end-to-end.
 */
public interface ConfirmacionPort {

    void confirmar(ConfirmacionPago datos);

    Optional<Pago> consultarPago(String reservaId);
}
