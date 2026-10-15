package com.stagepass.live.adapter.out.notificacion;

import com.stagepass.live.application.port.out.NotificacionPort;
import com.stagepass.live.domain.model.BoletoDigital;
import com.stagepass.live.domain.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ADAPTADOR SIMULADO de WhatsApp. No envia mensajes reales: solo registra en
 * el log la notificacion que se habria enviado al telefono movil del cliente.
 */
@Component
public class WhatsAppAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppAdapter.class);

    @Override
    public void enviar(Cliente cliente, List<BoletoDigital> boletos) {
        log.info("[SIMULACION WhatsApp] Mensaje a {} ({}): {} boleto(s) emitidos ({})",
                cliente.getNombreCompleto(), cliente.getTelefonoMovil(), boletos.size(),
                boletos.stream().map(BoletoDigital::getCodigoRedencion).toList());
    }
}
