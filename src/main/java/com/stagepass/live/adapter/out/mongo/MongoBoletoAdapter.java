package com.stagepass.live.adapter.out.mongo;

import com.stagepass.live.adapter.out.mongo.document.BoletoDocument;
import com.stagepass.live.adapter.out.mongo.document.ReservaDocument;
import com.stagepass.live.application.port.out.BoletoRepository;
import com.stagepass.live.domain.model.BoletoDigital;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Adaptador de salida REAL hacia MongoDB para la persistencia de boletos.
 *
 * guardarSinDuplicar aprovecha el indice unico del "_id" (reservaId::asientoId)
 * para que una insercion repetida (mismo webhook procesado dos veces) sea un
 * no-op silencioso en lugar de una emision duplicada.
 */
@Repository
public class MongoBoletoAdapter implements BoletoRepository {

    private final MongoTemplate mongoTemplate;

    public MongoBoletoAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void guardarSinDuplicar(List<BoletoDigital> boletos) {
        if (boletos.isEmpty()) {
            return;
        }
        String reservaId = boletos.get(0).getReservaId();
        ReservaDocument reserva = mongoTemplate.findById(reservaId, ReservaDocument.class);
        String eventoId = reserva != null ? reserva.getEventoId() : null;
        String clienteId = reserva != null ? reserva.getClienteId() : null;

        for (BoletoDigital boleto : boletos) {
            String id = BoletoDocument.construirId(boleto.getReservaId(), boleto.getAsientoId());
            BoletoDocument documento = new BoletoDocument(id, boleto.getId(), boleto.getReservaId(),
                    boleto.getAsientoId(), clienteId, eventoId, boleto.getCodigoRedencion(),
                    boleto.getFirmaHash(), boleto.getFechaEmision());
            try {
                mongoTemplate.insert(documento);
            } catch (DuplicateKeyException ex) {
                // Ya existe un boleto para este asiento+reserva: emision idempotente, no se duplica.
            }
        }
    }

    @Override
    public List<BoletoDigital> porReserva(String reservaId) {
        Query query = Query.query(where("reservaId").is(reservaId));
        return mongoTemplate.find(query, BoletoDocument.class).stream().map(this::aDominio).toList();
    }

    @Override
    public Optional<BoletoDigital> buscarPorId(String id) {
        Query query = Query.query(where("boletoId").is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, BoletoDocument.class)).map(this::aDominio);
    }

    @Override
    public int contarEmitidos(String clienteId, String eventoId) {
        Query query = Query.query(where("clienteId").is(clienteId).and("eventoId").is(eventoId));
        return (int) mongoTemplate.count(query, BoletoDocument.class);
    }

    private BoletoDigital aDominio(BoletoDocument documento) {
        return new BoletoDigital(documento.getBoletoId(), documento.getReservaId(), documento.getAsientoId(),
                documento.getCodigoRedencion(), documento.getFechaEmision(), documento.getFirmaHash());
    }
}
