package com.stagepass.live.domain.exception;

public class EventoNoDisponibleException extends ReglaNegocioException {

    public EventoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
