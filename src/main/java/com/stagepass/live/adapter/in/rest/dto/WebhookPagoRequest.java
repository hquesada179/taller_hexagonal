package com.stagepass.live.adapter.in.rest.dto;

import com.stagepass.live.domain.enums.ProveedorPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cuerpo generico de un webhook de confirmacion de pago, tal como lo
 * enviaria una pasarela real: siempre incluye el instante en que EL
 * PROVEEDOR resolvio la transaccion (no se confia en la hora de recepcion).
 */
public record WebhookPagoRequest(
        @NotBlank String reservaId,
        @NotBlank String referencia,
        @NotNull ProveedorPago proveedor,
        @NotNull BigDecimal monto,
        boolean exitoso,
        @NotNull LocalDateTime momentoConfirmacionProveedor) {
}
