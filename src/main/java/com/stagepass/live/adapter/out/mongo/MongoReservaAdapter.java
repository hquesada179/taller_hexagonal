package com.stagepass.live.adapter.out.mongo;

import com.mongodb.client.result.UpdateResult;
import com.stagepass.live.adapter.out.mongo.document.ReservaDocument;
import com.stagepass.live.application.port.out.ReservaRepository;
import com.stagepass.live.domain.enums.EstadoReserva;
import com.stagepass.live.domain.model.Reserva;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Adaptador de salida REAL hacia MongoDB para la persistencia de reservas.
 *
 * aprobarSiVigente / expirarSiPendiente son actualizaciones atomicas a nivel
 * de documento (operacion nativa de MongoDB), que es lo que garantiza que un
 * webhook duplicado o una carrera con el scheduler de expiracion nunca
 * produzcan una doble aprobacion ni una aprobacion de una reserva ya vencida.
 */
@Repository
public class MongoReservaAdapter implements ReservaRepository {

    private final MongoTemplate mongoTemplate;

    public MongoReservaAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void guardar(Reserva reserva) {
        ReservaDocument documento = new ReservaDocument(
                reserva.getId(), reserva.getEventoId(), reserva.getClienteId(), reserva.getAsientoIds(),
                reserva.getSubtotal(), reserva.getTarifaServicio(), reserva.getDescuento(), reserva.getTotal(),
                reserva.getExpiraEn(), reserva.getEstado().name());
        mongoTemplate.save(documento);
    }

    @Override
    public Optional<Reserva> buscar(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, ReservaDocument.class)).map(this::aDominio);
    }

    @Override
    public List<Reserva> vencidas(LocalDateTime ahora) {
        Query query = Query.query(where("estado").is(EstadoReserva.PENDIENTE_PAGO.name())
                .and("expiraEn").lte(ahora));
        return mongoTemplate.find(query, ReservaDocument.class).stream().map(this::aDominio).toList();
    }

    @Override
    public boolean aprobarSiVigente(String id, LocalDateTime instante) {
        Query query = Query.query(where("_id").is(id)
                .and("estado").is(EstadoReserva.PENDIENTE_PAGO.name())
                .and("expiraEn").gt(instante));
        Update update = Update.update("estado", EstadoReserva.APROBADA.name());
        UpdateResult resultado = mongoTemplate.updateFirst(query, update, ReservaDocument.class);
        return resultado.getModifiedCount() > 0;
    }

    @Override
    public boolean expirarSiPendiente(String id, LocalDateTime ahora) {
        Query query = Query.query(where("_id").is(id)
                .and("estado").is(EstadoReserva.PENDIENTE_PAGO.name())
                .and("expiraEn").lte(ahora));
        Update update = Update.update("estado", EstadoReserva.EXPIRADA.name());
        UpdateResult resultado = mongoTemplate.updateFirst(query, update, ReservaDocument.class);
        return resultado.getModifiedCount() > 0;
    }

    @Override
    public int contarReservados(String clienteId, String eventoId) {
        // IMPORTANTE: debe contar BOLETOS (asientos) reservados, no documentos de
        // reserva. Una sola reserva PENDIENTE_PAGO puede tener varios asientos
        // (asientoIds), y PoliticaLimite suma este valor con "emitidos" y "nuevos"
        // en unidades de boletos. Contar documentos en vez de asientos subestimaria
        // el total y permitiria superar el limite de 6 boletos por cliente/evento.
        Query query = Query.query(where("clienteId").is(clienteId)
                .and("eventoId").is(eventoId)
                .and("estado").is(EstadoReserva.PENDIENTE_PAGO.name()));
        return mongoTemplate.find(query, ReservaDocument.class).stream()
                .mapToInt(documento -> documento.getAsientoIds().size())
                .sum();
    }

    private Reserva aDominio(ReservaDocument documento) {
        return new Reserva(documento.getId(), documento.getEventoId(), documento.getClienteId(),
                documento.getAsientoIds(), documento.getSubtotal(), documento.getTarifaServicio(),
                documento.getDescuento(), documento.getTotal(), documento.getExpiraEn(),
                EstadoReserva.valueOf(documento.getEstado()));
    }
}
