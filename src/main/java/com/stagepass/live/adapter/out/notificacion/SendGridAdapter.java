package com.stagepass.live.adapter.out.notificacion;

import com.stagepass.live.application.port.out.NotificacionPort;
import com.stagepass.live.domain.model.BoletoDigital;
import com.stagepass.live.domain.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ADAPTADOR SIMULADO de SendGrid (correo electronico). No envia correos
 * reales: solo registra en el log la notificacion que se habria enviado.
 */
@Component
public class SendGridAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(SendGridAdapter.class);

    @Override
    public void enviar(Cliente cliente, List<BoletoDigital> boletos) {
        log.info("[SIMULACION SendGrid] Correo a {} <{}>: {} boleto(s) adjuntos ({})",
                cliente.getNombreCompleto(), cliente.getCorreo(), boletos.size(),
                boletos.stream().map(BoletoDigital::getCodigoRedencion).toList());
    }
}
