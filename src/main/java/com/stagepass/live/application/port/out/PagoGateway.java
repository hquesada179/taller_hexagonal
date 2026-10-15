package com.stagepass.live.application.port.out;

import com.stagepass.live.domain.model.Pago;
import com.stagepass.live.domain.model.Reserva;
import com.stagepass.live.domain.enums.ProveedorPago;
import com.stagepass.live.domain.vo.ConfirmacionPago;

import java.util.Optional;

/**
 * Puerto de salida hacia la pasarela de pago. Implementado por un router
 * (PagoRouter) que selecciona entre PayPal, Wompi o PSE (todos simulados).
 *
 * "consultar" es una extension respecto del UML original (que solo definia
 * registrar/verificar): se agrega para poder exponer por API el estado y la
 * referencia externa de un pago ya registrado, necesarios para demostrar el
 * flujo de pago simulado sin exponer secretos del dominio.
 */
public interface PagoGateway {

    Pago registrar(Reserva reserva, ProveedorPago proveedor);

    Pago verificar(ConfirmacionPago datos);

    Optional<Pago> consultar(String reservaId);
}
