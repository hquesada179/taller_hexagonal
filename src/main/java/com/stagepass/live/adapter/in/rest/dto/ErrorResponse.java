package com.stagepass.live.adapter.in.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, List<String> detalles) {

    public static ErrorResponse de(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, List.of());
    }

    public static ErrorResponse de(int status, String error, String message, List<String> detalles) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, detalles);
    }
}
