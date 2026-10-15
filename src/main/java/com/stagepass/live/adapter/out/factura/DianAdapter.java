package com.stagepass.live.adapter.out.factura;

import com.stagepass.live.application.port.out.FacturaPort;
import com.stagepass.live.domain.model.Cliente;
import com.stagepass.live.domain.model.Reserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ADAPTADOR SIMULADO de facturacion electronica DIAN. No se conecta con los
 * servicios reales de la DIAN: genera un identificador de factura (similar a
 * un CUFE) y registra la emision en el log, solo para fines de demostracion.
 */
@Component
public class DianAdapter implements FacturaPort {

    private static final Logger log = LoggerFactory.getLogger(DianAdapter.class);

    @Override
    public String emitir(Reserva reserva, Cliente cliente) {
        String cufeSimulado = "CUFE-SIM-" + UUID.randomUUID();
        log.info("[SIMULACION DIAN] Factura electronica emitida para la reserva {} (cliente {}): total={} -> {}",
                reserva.getId(), cliente.getId(), reserva.getTotal(), cufeSimulado);
        return cufeSimulado;
    }
}
