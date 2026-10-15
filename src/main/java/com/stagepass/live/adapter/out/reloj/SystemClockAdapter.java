package com.stagepass.live.adapter.out.reloj;

import com.stagepass.live.application.port.out.RelojPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Adaptador de salida REAL del reloj: delega en el reloj del sistema.
 * En pruebas se sustituye por un reloj fijo/controlable (ver RelojPort).
 */
@Component
public class SystemClockAdapter implements RelojPort {

    @Override
    public LocalDateTime ahora() {
        return LocalDateTime.now();
    }
}
