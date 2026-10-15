package com.stagepass.live.application.port.in;

import java.time.LocalDateTime;

/**
 * Resultado informativo de la ultima ejecucion del proceso de expiracion.
 * No es una entidad de dominio: es un dato de reporte para el caso de uso
 * "consultar el proceso de expiracion" exigido por el taller.
 */
public record ResultadoExpiracion(int reservasExpiradas, LocalDateTime ejecutadoEn) {

    public static ResultadoExpiracion ningunaEjecucion() {
        return new ResultadoExpiracion(0, null);
    }
}
