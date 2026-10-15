package com.stagepass.live.config;

import com.stagepass.live.adapter.out.mongo.document.BoletoDocument;
import com.stagepass.live.adapter.out.mongo.document.OcupacionDocument;
import com.stagepass.live.adapter.out.mongo.document.ReservaDocument;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

/**
 * Garantiza los indices unicos criticos de MongoDB en el arranque, de forma
 * que el proyecto funcione igual con el script de inicializacion de Docker
 * (mongo/init-mongo.js) o con una instancia de MongoDB levantada manualmente.
 */
@Component
public class MongoIndexInitializer {

    private final MongoTemplate mongoTemplate;

    public MongoIndexInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void asegurarIndices() {
        mongoTemplate.indexOps(OcupacionDocument.class)
                .ensureIndex(new Index().on("eventoId", org.springframework.data.domain.Sort.Direction.ASC)
                        .on("asientoId", org.springframework.data.domain.Sort.Direction.ASC)
                        .named("ix_evento_asiento"));

        mongoTemplate.indexOps(BoletoDocument.class)
                .ensureIndex(new Index().on("codigoRedencion", org.springframework.data.domain.Sort.Direction.ASC)
                        .unique().named("uq_codigo_redencion"));
        mongoTemplate.indexOps(BoletoDocument.class)
                .ensureIndex(new Index().on("boletoId", org.springframework.data.domain.Sort.Direction.ASC)
                        .unique().named("uq_boleto_id"));

        mongoTemplate.indexOps(ReservaDocument.class)
                .ensureIndex(new Index().on("clienteId", org.springframework.data.domain.Sort.Direction.ASC)
                        .on("eventoId", org.springframework.data.domain.Sort.Direction.ASC)
                        .named("ix_cliente_evento"));
        mongoTemplate.indexOps(ReservaDocument.class)
                .ensureIndex(new Index().on("estado", org.springframework.data.domain.Sort.Direction.ASC)
                        .on("expiraEn", org.springframework.data.domain.Sort.Direction.ASC)
                        .named("ix_estado_expira"));
    }
}
