package com.stagepass.live.application.port.in;

import com.stagepass.live.domain.enums.ProveedorPago;
import com.stagepass.live.domain.model.Reserva;

import java.util.List;

/**
 * Puerto de entrada para iniciar y consultar una compra.
 *
 * "consultar" es una extension respecto del UML original (que solo definia
 * iniciar); se agrega para satisfacer el requisito del taller de exponer un
 * endpoint de "consultar una reserva" sin crear un puerto adicional.
 */
public interface CompraPort {

    Reserva iniciar(String clienteId, String eventoId, List<String> asientoIds, ProveedorPago proveedor);

    Reserva consultar(String reservaId);
}
