package com.stagepass.live.domain.model;

import com.stagepass.live.domain.enums.EstadoEvento;
import com.stagepass.live.domain.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.Objects;

public class Evento {

    private final String id;
    private final String nombre;
    private final TipoEvento tipo;
    private final String recintoId;
    private final LocalDateTime fechaHora;
    private final EstadoEvento estado;

    public Evento(String id, String nombre, TipoEvento tipo, String recintoId,
                   LocalDateTime fechaHora, EstadoEvento estado) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.nombre = Objects.requireNonNull(nombre, "nombre es obligatorio");
        this.tipo = Objects.requireNonNull(tipo, "tipo es obligatorio");
        this.recintoId = Objects.requireNonNull(recintoId, "recintoId es obligatorio");
        this.fechaHora = Objects.requireNonNull(fechaHora, "fechaHora es obligatoria");
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
    }

    public boolean estaProgramado() {
        return estado == EstadoEvento.PROGRAMADO;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Evento evento)) return false;
        return Objects.equals(id, evento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
