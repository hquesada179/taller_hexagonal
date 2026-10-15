package com.stagepass.live.application.port.out;

import com.stagepass.live.domain.model.Reserva;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida hacia la persistencia de reservas.
 * Implementado realmente por MongoReservaAdapter.
 *
 * aprobarSiVigente/expirarSiPendiente son operaciones ATOMICAS a nivel de
 * base de datos (no "leer + validar en memoria + escribir"), necesarias para
 * que webhooks duplicados o el scheduler de expiracion compitiendo con una
 * confirmacion de pago no produzcan estados inconsistentes.
 */
public interface ReservaRepository {

    void guardar(Reserva reserva);

    Optional<Reserva> buscar(String id);

    List<Reserva> vencidas(LocalDateTime ahora);

    boolean aprobarSiVigente(String id, LocalDateTime instante);

    boolean expirarSiPendiente(String id, LocalDateTime ahora);

    /**
     * Cantidad de BOLETOS/ASIENTOS (no de documentos de reserva) que el
     * cliente tiene actualmente en reservas PENDIENTE_PAGO para el evento.
     * Una sola reserva puede contener varios asientos; el resultado debe ser
     * la suma de asientos de todas sus reservas pendientes, para ser
     * comparable en las mismas unidades que "emitidos" (boletos) dentro de
     * PoliticaLimite.
     */
    int contarReservados(String clienteId, String eventoId);
}
