package com.stagepass.live.adapter.out.mysql.entity;

import com.stagepass.live.domain.enums.EstadoEvento;
import com.stagepass.live.domain.enums.TipoEvento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidad JPA de infraestructura. Reutiliza los enumerados del dominio
 * (TipoEvento, EstadoEvento) solo como tipos de columna: el adaptador puede
 * depender del dominio (la flecha de dependencia va hacia adentro), pero el
 * dominio nunca depende de esta clase ni de JPA.
 */
@Entity
@Table(name = "eventos")
public class EventoEntity {

    @Id
    @Column(name = "id", length = 40, nullable = false)
    private String id;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoEvento tipo;

    @Column(name = "recinto_id", length = 40, nullable = false)
    private String recintoId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoEvento estado;

    protected EventoEntity() {
    }

    public EventoEntity(String id, String nombre, TipoEvento tipo, String recintoId,
                         LocalDateTime fechaHora, EstadoEvento estado) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.recintoId = recintoId;
        this.fechaHora = fechaHora;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public String getRecintoId() {
        return recintoId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public EstadoEvento getEstado() {
        return estado;
    }
}
