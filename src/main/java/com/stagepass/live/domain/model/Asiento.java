package com.stagepass.live.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Asiento {

    private final String id;
    private final String recintoId;
    private final String codigo;
    private final BigDecimal precioBase;

    public Asiento(String id, String recintoId, String codigo, BigDecimal precioBase) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.recintoId = Objects.requireNonNull(recintoId, "recintoId es obligatorio");
        this.codigo = Objects.requireNonNull(codigo, "codigo es obligatorio");
        this.precioBase = Objects.requireNonNull(precioBase, "precioBase es obligatorio");
        if (precioBase.signum() < 0) {
            throw new IllegalArgumentException("precioBase no puede ser negativo");
        }
    }

    public String getId() {
        return id;
    }

    public String getRecintoId() {
        return recintoId;
    }

    public String getCodigo() {
        return codigo;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asiento asiento)) return false;
        return Objects.equals(id, asiento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
