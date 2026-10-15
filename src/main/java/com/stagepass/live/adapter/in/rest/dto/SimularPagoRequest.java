package com.stagepass.live.adapter.in.rest.dto;

/**
 * Peticion del endpoint de demostracion que simula el resultado de un pago
 * sin que el cliente necesite conocer la referencia externa generada por la
 * pasarela: el servidor la recupera internamente a partir de la reserva.
 */
public record SimularPagoRequest(boolean exitoso) {
}
