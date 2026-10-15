package com.stagepass.live.domain.exception;

/**
 * Se lanza cuando se solicita por identificador una entidad que no existe
 * (evento, recinto, reserva, cliente, etc). No es en si misma una violacion
 * de regla de negocio, pero se mapea igualmente a un codigo HTTP en el borde
 * de la aplicacion.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
