package com.stagepass.live.adapter.in.rest.dto;

import com.stagepass.live.domain.model.BoletoDigital;

import java.time.LocalDateTime;

public record BoletoResponse(String id, String reservaId, String asientoId, String codigoRedencion,
                              String firmaHash, LocalDateTime fechaEmision) {

    public static BoletoResponse desde(BoletoDigital boleto) {
        return new BoletoResponse(boleto.getId(), boleto.getReservaId(), boleto.getAsientoId(),
                boleto.getCodigoRedencion(), boleto.getFirmaHash(), boleto.getFechaEmision());
    }
}
