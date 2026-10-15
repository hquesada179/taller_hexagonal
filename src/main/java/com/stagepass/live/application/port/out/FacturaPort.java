package com.stagepass.live.application.port.out;

import com.stagepass.live.domain.model.Cliente;
import com.stagepass.live.domain.model.Reserva;

/**
 * Puerto de salida hacia la facturacion electronica (simulada: DianAdapter).
 * Retorna el identificador/CUFE de la factura emitida.
 */
public interface FacturaPort {

    String emitir(Reserva reserva, Cliente cliente);
}
