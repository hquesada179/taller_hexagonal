package com.stagepass.live.adapter.in.rest.dto;

import com.stagepass.live.domain.enums.EstadoPago;
import com.stagepass.live.domain.enums.ProveedorPago;
import com.stagepass.live.domain.model.Pago;

import java.math.BigDecimal;

public record PagoResponse(String id, String reservaId, String referenciaExterna, BigDecimal monto,
                            EstadoPago estado, ProveedorPago proveedor) {

    public static PagoResponse desde(Pago pago) {
        return new PagoResponse(pago.getId(), pago.getReservaId(), pago.getReferenciaExterna(),
                pago.getMonto(), pago.getEstado(), pago.getProveedor());
    }
}
