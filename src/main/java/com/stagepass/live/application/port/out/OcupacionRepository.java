package com.stagepass.live.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Puerto de salida hacia la ocupacion de asientos por funcion (evento).
 * Implementado realmente por MongoOcupacionAdapter con un indice unico
 * (eventoId, asientoId) que impide fisicamente vender dos veces el mismo
 * asiento de una funcion, incluso bajo solicitudes concurrentes.
 */
public interface OcupacionRepository {

    /**
     * Intenta bloquear atomicamente TODOS los asientos indicados para el evento.
     * Si alguno ya esta BLOQUEADO o VENDIDO, no bloquea ninguno y retorna false.
     */
    boolean reservarAtomico(String eventoId, List<String> asientoIds, String reservaId,
                             LocalDateTime hasta, String clienteId);

    void liberar(String reservaId);

    void confirmarVenta(String reservaId);
}
