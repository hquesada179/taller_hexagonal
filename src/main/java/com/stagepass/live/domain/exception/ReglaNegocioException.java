package com.stagepass.live.domain.exception;

/**
 * Excepcion base para toda violacion de una regla de negocio del dominio.
 * No depende de HTTP ni de ningun framework: los adaptadores de entrada
 * son responsables de traducirla al protocolo correspondiente.
 */
public abstract class ReglaNegocioException extends RuntimeException {

    protected ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
