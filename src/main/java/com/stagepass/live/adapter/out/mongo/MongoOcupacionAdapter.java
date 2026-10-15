package com.stagepass.live.adapter.out.mongo;

import com.stagepass.live.adapter.out.mongo.document.OcupacionDocument;
import com.stagepass.live.application.port.out.OcupacionRepository;
import com.stagepass.live.domain.enums.EstadoAsiento;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Adaptador de salida REAL hacia MongoDB para la ocupacion de asientos.
 *
 * Estrategia de concurrencia (sin depender de transacciones multi-documento,
 * que requieren un replica set): para cada asiento se intenta primero un
 * INSERT puro con un "_id" deterministico (eventoId::asientoId). Si ya existe
 * un documento para ese asiento+evento, MongoDB rechaza el insert con un
 * error de clave duplicada (indice unico de "_id"); en ese caso se intenta
 * una actualizacion condicional que solo tiene exito si el asiento esta
 * DISPONIBLE o su bloqueo anterior ya expiro. Si cualquier asiento de la
 * solicitud no puede bloquearse, se liberan los que si se alcanzaron a
 * bloquear en este mismo intento (mismo reservaId) y se informa el fallo.
 * Esto es lo que demuestra, en la practica, que nunca se vende dos veces el
 * mismo asiento de una funcion.
 */
@Repository
public class MongoOcupacionAdapter implements OcupacionRepository {

    private final MongoTemplate mongoTemplate;

    public MongoOcupacionAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean reservarAtomico(String eventoId, List<String> asientoIds, String reservaId,
                                    LocalDateTime hasta, String clienteId) {
        // Todo el bucle se protege con try/catch: si CUALQUIER asiento falla (ya tomado) o
        // lanza una excepcion inesperada (p. ej. un error transitorio de MongoDB) a mitad de
        // la solicitud, los asientos ya bloqueados en ESTE intento (mismo reservaId) se liberan
        // siempre. Sin este catch, una excepcion a mitad del bucle dejaria esos asientos
        // bloqueados hasta que su "bloqueadoHasta" expirara por si solo.
        try {
            for (String asientoId : asientoIds) {
                String id = OcupacionDocument.construirId(eventoId, asientoId);
                boolean bloqueado = intentarInsertar(id, eventoId, asientoId, reservaId, clienteId, hasta)
                        || intentarActualizarSiDisponible(id, reservaId, clienteId, hasta);

                if (!bloqueado) {
                    liberar(reservaId);
                    return false;
                }
            }
            return true;
        } catch (RuntimeException ex) {
            liberar(reservaId);
            throw ex;
        }
    }

    private boolean intentarInsertar(String id, String eventoId, String asientoId, String reservaId,
                                       String clienteId, LocalDateTime hasta) {
        try {
            OcupacionDocument documento = new OcupacionDocument(
                    id, eventoId, asientoId, EstadoAsiento.BLOQUEADO.name(), reservaId, clienteId, hasta);
            mongoTemplate.insert(documento);
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }

    private boolean intentarActualizarSiDisponible(String id, String reservaId, String clienteId, LocalDateTime hasta) {
        LocalDateTime ahora = LocalDateTime.now();
        Query query = Query.query(where("_id").is(id).orOperator(
                where("estado").is(EstadoAsiento.DISPONIBLE.name()),
                where("estado").is(EstadoAsiento.BLOQUEADO.name()).and("bloqueadoHasta").lt(ahora)));
        Update update = new Update()
                .set("estado", EstadoAsiento.BLOQUEADO.name())
                .set("reservaId", reservaId)
                .set("clienteId", clienteId)
                .set("bloqueadoHasta", hasta);
        return mongoTemplate.updateFirst(query, update, OcupacionDocument.class).getModifiedCount() > 0;
    }

    @Override
    public void liberar(String reservaId) {
        Query query = Query.query(where("reservaId").is(reservaId));
        Update update = new Update()
                .set("estado", EstadoAsiento.DISPONIBLE.name())
                .unset("reservaId")
                .unset("clienteId")
                .unset("bloqueadoHasta");
        mongoTemplate.updateMulti(query, update, OcupacionDocument.class);
    }

    @Override
    public void confirmarVenta(String reservaId) {
        Query query = Query.query(where("reservaId").is(reservaId).and("estado").is(EstadoAsiento.BLOQUEADO.name()));
        Update update = new Update()
                .set("estado", EstadoAsiento.VENDIDO.name())
                .unset("bloqueadoHasta");
        mongoTemplate.updateMulti(query, update, OcupacionDocument.class);
    }
}
