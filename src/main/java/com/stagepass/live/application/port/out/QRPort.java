package com.stagepass.live.application.port.out;

import com.stagepass.live.domain.model.BoletoDigital;

/**
 * Puerto de salida para la generacion del codigo QR de un boleto.
 * Retorna la imagen codificada en base64 (data URI) lista para incrustar o descargar.
 */
public interface QRPort {

    String generar(BoletoDigital boleto);
}
