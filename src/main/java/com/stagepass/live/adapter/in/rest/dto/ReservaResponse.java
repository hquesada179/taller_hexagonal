package com.stagepass.live.adapter.in.rest.dto;

import com.stagepass.live.domain.enums.EstadoReserva;
import com.stagepass.live.domain.model.Reserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReservaResponse(String id, String eventoId, String clienteId, List<String> asientoIds,
                               BigDecimal subtotal, BigDecimal tarifaServicio, BigDecimal descuento,
                               BigDecimal total, LocalDateTime expiraEn, EstadoReserva estado) {

    public static ReservaResponse desde(Reserva reserva) {
        return new ReservaResponse(reserva.getId(), reserva.getEventoId(), reserva.getClienteId(),
                reserva.getAsientoIds(), reserva.getSubtotal(), reserva.getTarifaServicio(),
                reserva.getDescuento(), reserva.getTotal(), reserva.getExpiraEn(), reserva.getEstado());
    }
}
