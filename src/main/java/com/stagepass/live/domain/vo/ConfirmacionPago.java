package com.stagepass.live.domain.vo;

import com.stagepass.live.domain.enums.ProveedorPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Datos que entrega la pasarela de pago (via webhook o verificacion directa)
 * sobre el resultado de un intento de pago. "momentoConfirmacionProveedor" es
 * el instante real en que el PROVEEDOR resolvio la transaccion (no el instante
 * en que nuestro servidor recibe el webhook), que es lo que exige el enunciado
 * para validar la vigencia de la reserva. "exitoso" es una extension respecto
 * del UML original: un webhook real de pago siempre informa un resultado
 * (aprobado o rechazado), no solo una confirmacion positiva.
 */
public final class ConfirmacionPago {

    private final String reservaId;
    private final String referencia;
    private final ProveedorPago proveedor;
    private final BigDecimal monto;
    private final boolean exitoso;
    private final LocalDateTime momentoConfirmacionProveedor;

    public ConfirmacionPago(String reservaId, String referencia, ProveedorPago proveedor,
                             BigDecimal monto, boolean exitoso, LocalDateTime momentoConfirmacionProveedor) {
        this.reservaId = Objects.requireNonNull(reservaId);
        this.referencia = Objects.requireNonNull(referencia);
        this.proveedor = Objects.requireNonNull(proveedor);
        this.monto = Objects.requireNonNull(monto);
        this.exitoso = exitoso;
        this.momentoConfirmacionProveedor = Objects.requireNonNull(momentoConfirmacionProveedor);
    }

    public String getReservaId() {
        return reservaId;
    }

    public String getReferencia() {
        return referencia;
    }

    public ProveedorPago getProveedor() {
        return proveedor;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public LocalDateTime getMomentoConfirmacionProveedor() {
        return momentoConfirmacionProveedor;
    }
}
