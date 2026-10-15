package com.stagepass.live.testutil;

import com.stagepass.live.application.port.out.RelojPort;

import java.time.LocalDateTime;

/**
 * Reloj controlable para pruebas: permite fijar y avanzar el "ahora" sin
 * depender del reloj real del sistema ni usar Thread.sleep.
 */
public class RelojFijo implements RelojPort {

    private LocalDateTime instante;

    public RelojFijo(LocalDateTime instanteInicial) {
        this.instante = instanteInicial;
    }

    @Override
    public LocalDateTime ahora() {
        return instante;
    }

    public void avanzar(long minutos) {
        this.instante = this.instante.plusMinutes(minutos);
    }

    public void fijar(LocalDateTime nuevoInstante) {
        this.instante = nuevoInstante;
    }
}
