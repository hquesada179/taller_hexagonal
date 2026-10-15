package com.stagepass.live.application.port.out;

/**
 * Puerto de salida para la firma criptografica (HMAC-SHA256) de boletos.
 * La clave secreta vive en infraestructura/configuracion (nunca en el dominio).
 */
public interface FirmaPort {

    String firmar(String datos, String eventoId);

    boolean verificar(String datos, String firma, String eventoId);
}
