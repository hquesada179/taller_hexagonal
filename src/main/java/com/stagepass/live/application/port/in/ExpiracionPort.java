package com.stagepass.live.application.port.in;

/**
 * Puerto de entrada para el proceso de expiracion de reservas vencidas.
 *
 * "consultarUltimaEjecucion" es una extension respecto del UML original
 * (que solo definia expirarVencidas); se agrega para poder exponer por API
 * el resultado de la ultima corrida, sin inventar un puerto adicional.
 */
public interface ExpiracionPort {

    int expirarVencidas();

    ResultadoExpiracion consultarUltimaEjecucion();
}
