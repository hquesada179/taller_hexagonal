package com.stagepass.live.application.port.out;

import com.stagepass.live.domain.model.BoletoDigital;
import com.stagepass.live.domain.model.Cliente;

import java.util.List;

/**
 * Puerto de salida para notificar al cliente la emision de sus boletos.
 * Implementado por un adaptador compuesto que envia por correo (SendGrid)
 * y por WhatsApp (ambos simulados).
 */
public interface NotificacionPort {

    void enviar(Cliente cliente, List<BoletoDigital> boletos);
}
