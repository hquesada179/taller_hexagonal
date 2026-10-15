package com.stagepass.live.domain.model;

import com.stagepass.live.domain.enums.EstadoPago;
import com.stagepass.live.domain.enums.ProveedorPago;

import java.math.BigDecimal;
import java.util.Objects;

public class Pago {

    private final String id;
    private final String reservaId;
    private final String referenciaExterna;
    private final BigDecimal monto;
    private EstadoPago estado;
    private final ProveedorPago proveedor;

    public Pago(String id, String reservaId, String referenciaExterna, BigDecimal monto,
                EstadoPago estado, ProveedorPago proveedor) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.reservaId = Objects.requireNonNull(reservaId, "reservaId es obligatorio");
        this.referenciaExterna = Objects.requireNonNull(referenciaExterna, "referenciaExterna es obligatoria");
        this.monto = Objects.requireNonNull(monto, "monto es obligatorio");
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
        this.proveedor = Objects.requireNonNull(proveedor, "proveedor es obligatorio");
    }

    public boolean estaVerificado() {
        return estado == EstadoPago.APROBADO;
    }

    public void aprobar() {
        this.estado = EstadoPago.APROBADO;
    }

    public void rechazar() {
        this.estado = EstadoPago.RECHAZADO;
    }

    public String getId() {
        return id;
    }

    public String getReservaId() {
        return reservaId;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public ProveedorPago getProveedor() {
        return proveedor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pago pago)) return false;
        return Objects.equals(id, pago.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
