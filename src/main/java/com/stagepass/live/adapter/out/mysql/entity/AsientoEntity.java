package com.stagepass.live.adapter.out.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "asientos")
public class AsientoEntity {

    @Id
    @Column(name = "id", length = 40, nullable = false)
    private String id;

    @Column(name = "recinto_id", length = 40, nullable = false)
    private String recintoId;

    @Column(name = "codigo", length = 20, nullable = false)
    private String codigo;

    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    protected AsientoEntity() {
    }

    public AsientoEntity(String id, String recintoId, String codigo, BigDecimal precioBase) {
        this.id = id;
        this.recintoId = recintoId;
        this.codigo = codigo;
        this.precioBase = precioBase;
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
}
