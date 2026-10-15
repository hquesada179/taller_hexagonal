package com.stagepass.live.adapter.in.scheduler;

import com.stagepass.live.application.port.in.ExpiracionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada programado: ejecuta periodicamente el caso de uso de
 * expiracion para liberar automaticamente los asientos de reservas cuyo
 * bloqueo de 10 minutos ya vencio sin pago confirmado.
 */
@Component
public class ReservaScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservaScheduler.class);

    private final ExpiracionPort expiracionPort;

    public ReservaScheduler(ExpiracionPort expiracionPort) {
        this.expiracionPort = expiracionPort;
    }

    @Scheduled(fixedRateString = "${stagepass.expiracion.scheduler-fijo-ms}")
    public void ejecutar() {
        int expiradas = expiracionPort.expirarVencidas();
        if (expiradas > 0) {
            log.info("Proceso de expiracion: {} reserva(s) liberada(s) automaticamente", expiradas);
        }
    }
}
