package com.stagepass.live.adapter.in.rest.dto;

import com.stagepass.live.domain.model.Asiento;

import java.math.BigDecimal;

public record AsientoResponse(String id, String recintoId, String codigo, BigDecimal precioBase) {

    public static AsientoResponse desde(Asiento asiento) {
        return new AsientoResponse(asiento.getId(), asiento.getRecintoId(), asiento.getCodigo(), asiento.getPrecioBase());
    }
}
