package com.stagepass.live.adapter.out.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA de infraestructura. Es un simple mapeo de columnas: no contiene
 * ninguna regla de negocio (esas viven exclusivamente en el dominio).
 */
@Entity
@Table(name = "recintos")
public class RecintoEntity {

    @Id
    @Column(name = "id", length = 40, nullable = false)
    private String id;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "aforo", nullable = false)
    private int aforo;

    protected RecintoEntity() {
        // requerido por JPA
    }

    public RecintoEntity(String id, String nombre, int aforo) {
        this.id = id;
        this.nombre = nombre;
        this.aforo = aforo;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getAforo() {
        return aforo;
    }
}
