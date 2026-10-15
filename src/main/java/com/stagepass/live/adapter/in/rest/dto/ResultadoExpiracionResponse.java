package com.stagepass.live.adapter.in.rest.dto;

import com.stagepass.live.application.port.in.ResultadoExpiracion;

import java.time.LocalDateTime;

public record ResultadoExpiracionResponse(int reservasExpiradas, LocalDateTime ejecutadoEn) {

    public static ResultadoExpiracionResponse desde(ResultadoExpiracion resultado) {
        return new ResultadoExpiracionResponse(resultado.reservasExpiradas(), resultado.ejecutadoEn());
    }
}
