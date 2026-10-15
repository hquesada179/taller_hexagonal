package com.stagepass.live.application.port.out;

import java.time.LocalDateTime;

/**
 * Abstraccion del reloj del sistema. Permite inyectar un reloj controlable
 * en las pruebas (por ejemplo, para simular el paso del tiempo y verificar
 * la expiracion de reservas) sin depender de LocalDateTime.now() directamente
 * en los casos de uso.
 */
public interface RelojPort {

    LocalDateTime ahora();
}
