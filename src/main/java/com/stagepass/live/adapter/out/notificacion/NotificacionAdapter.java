package com.stagepass.live.adapter.out.notificacion;

import com.stagepass.live.application.port.out.NotificacionPort;
import com.stagepass.live.domain.model.BoletoDigital;
import com.stagepass.live.domain.model.Cliente;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador compuesto: es el UNICO bean de tipo NotificacionPort inyectado
 * en los casos de uso. Envia la notificacion tanto por correo (SendGrid)
 * como por WhatsApp (ambos simulados), ya que Cliente siempre tiene correo
 * y telefono movil.
 */
@Component
@Primary
public class NotificacionAdapter implements NotificacionPort {

    private final SendGridAdapter sendGridAdapter;
    private final WhatsAppAdapter whatsAppAdapter;

    public NotificacionAdapter(SendGridAdapter sendGridAdapter, WhatsAppAdapter whatsAppAdapter) {
        this.sendGridAdapter = sendGridAdapter;
        this.whatsAppAdapter = whatsAppAdapter;
    }

    @Override
    public void enviar(Cliente cliente, List<BoletoDigital> boletos) {
        sendGridAdapter.enviar(cliente, boletos);
        whatsAppAdapter.enviar(cliente, boletos);
    }
}
