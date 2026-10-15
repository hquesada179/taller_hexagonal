package com.stagepass.live.domain.model;

import com.stagepass.live.domain.enums.EstadoReserva;
import com.stagepass.live.domain.exception.ReservaNoVigenteException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Agregado que representa una compra en curso. El UML del taller no incluye
 * un campo "subtotal" explicito en Reserva (si en Cotizacion); se agrega aqui
 * para poder reconstruir el desglose economico sin recalcularlo, y queda
 * documentado como una extension minima y coherente en el README.
 */
public class Reserva {

    private final String id;
    private final String eventoId;
    private final String clienteId;
    private final List<String> asientoIds;
    private final BigDecimal subtotal;
    private final BigDecimal tarifaServicio;
    private final BigDecimal descuento;
    private final BigDecimal total;
    private final LocalDateTime expiraEn;
    private EstadoReserva estado;

    public Reserva(String id, String eventoId, String clienteId, List<String> asientoIds,
                    BigDecimal subtotal, BigDecimal tarifaServicio, BigDecimal descuento, BigDecimal total,
                    LocalDateTime expiraEn, EstadoReserva estado) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.eventoId = Objects.requireNonNull(eventoId, "eventoId es obligatorio");
        this.clienteId = Objects.requireNonNull(clienteId, "clienteId es obligatorio");
        this.asientoIds = List.copyOf(Objects.requireNonNull(asientoIds, "asientoIds es obligatorio"));
        if (this.asientoIds.isEmpty()) {
            throw new IllegalArgumentException("Una reserva debe tener al menos un asiento");
        }
        this.subtotal = Objects.requireNonNull(subtotal);
        this.tarifaServicio = Objects.requireNonNull(tarifaServicio);
        this.descuento = Objects.requireNonNull(descuento);
        this.total = Objects.requireNonNull(total);
        this.expiraEn = Objects.requireNonNull(expiraEn, "expiraEn es obligatorio");
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
    }

    public boolean estaVigente(LocalDateTime ahora) {
        return estado == EstadoReserva.PENDIENTE_PAGO && ahora.isBefore(expiraEn);
    }

    public void aprobar() {
        if (estado != EstadoReserva.PENDIENTE_PAGO) {
            throw new ReservaNoVigenteException(
                    "La reserva " + id + " no esta pendiente de pago (estado actual: " + estado + ")");
        }
        this.estado = EstadoReserva.APROBADA;
    }

    public void rechazar() {
        if (estado != EstadoReserva.PENDIENTE_PAGO) {
            throw new ReservaNoVigenteException(
                    "La reserva " + id + " no esta pendiente de pago (estado actual: " + estado + ")");
        }
        this.estado = EstadoReserva.RECHAZADA;
    }

    public void expirar() {
        if (estado != EstadoReserva.PENDIENTE_PAGO) {
            throw new ReservaNoVigenteException(
                    "Solo una reserva pendiente de pago puede expirar (estado actual: " + estado + ")");
        }
        this.estado = EstadoReserva.EXPIRADA;
    }

    public int cantidadBoletos() {
        return asientoIds.size();
    }

    public String getId() {
        return id;
    }

    public String getEventoId() {
        return eventoId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public List<String> getAsientoIds() {
        return asientoIds;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTarifaServicio() {
        return tarifaServicio;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reserva reserva)) return false;
        return Objects.equals(id, reserva.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
