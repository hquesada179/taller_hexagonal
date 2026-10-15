package com.stagepass.live.domain.model;

import com.stagepass.live.domain.enums.EstadoAsiento;
import com.stagepass.live.domain.exception.AsientoNoDisponibleException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa la ocupacion de un asiento para una funcion (evento) concreta.
 * Es la entidad que materializa el bloqueo temporal exigido por el taller:
 * un mismo par (eventoId, asientoId) nunca puede estar BLOQUEADO o VENDIDO
 * para dos reservas distintas al mismo tiempo.
 */
public class DisponibilidadAsiento {

    private final String eventoId;
    private final String asientoId;
    private EstadoAsiento estado;
    private String reservaId;
    private LocalDateTime bloqueadoHasta;

    public DisponibilidadAsiento(String eventoId, String asientoId, EstadoAsiento estado,
                                  String reservaId, LocalDateTime bloqueadoHasta) {
        this.eventoId = Objects.requireNonNull(eventoId, "eventoId es obligatorio");
        this.asientoId = Objects.requireNonNull(asientoId, "asientoId es obligatorio");
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
        this.reservaId = reservaId;
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public static DisponibilidadAsiento disponible(String eventoId, String asientoId) {
        return new DisponibilidadAsiento(eventoId, asientoId, EstadoAsiento.DISPONIBLE, null, null);
    }

    public void bloquear(String reservaId, LocalDateTime hasta) {
        if (estado != EstadoAsiento.DISPONIBLE) {
            throw new AsientoNoDisponibleException(
                    "El asiento " + asientoId + " no esta disponible para el evento " + eventoId);
        }
        this.estado = EstadoAsiento.BLOQUEADO;
        this.reservaId = Objects.requireNonNull(reservaId, "reservaId es obligatorio");
        this.bloqueadoHasta = Objects.requireNonNull(hasta, "hasta es obligatorio");
    }

    public void liberar() {
        this.estado = EstadoAsiento.DISPONIBLE;
        this.reservaId = null;
        this.bloqueadoHasta = null;
    }

    public void vender() {
        if (estado != EstadoAsiento.BLOQUEADO) {
            throw new AsientoNoDisponibleException(
                    "El asiento " + asientoId + " no esta bloqueado por una reserva vigente");
        }
        this.estado = EstadoAsiento.VENDIDO;
        this.bloqueadoHasta = null;
    }

    public String getEventoId() {
        return eventoId;
    }

    public String getAsientoId() {
        return asientoId;
    }

    public EstadoAsiento getEstado() {
        return estado;
    }

    public String getReservaId() {
        return reservaId;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }
}
