package com.stagepass.live.adapter.out.mongo;

import com.mongodb.client.MongoClients;
import com.stagepass.live.domain.model.BoletoDigital;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integracion REAL contra MongoDB: demuestra que guardarSinDuplicar
 * es idempotente (un boleto por asiento+reserva) incluso si se invoca mas de
 * una vez con los mismos datos, tal como ocurriria con un webhook duplicado.
 */
@Testcontainers
class MongoBoletoAdapterIT {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    static MongoTemplate mongoTemplate;
    static MongoBoletoAdapter adapter;

    @BeforeAll
    static void conectar() {
        mongoTemplate = new MongoTemplate(MongoClients.create(MONGO.getReplicaSetUrl()), "stagepass-test");
        adapter = new MongoBoletoAdapter(mongoTemplate);
    }

    @AfterAll
    static void cerrar() {
        mongoTemplate.getDb().drop();
    }

    @BeforeEach
    void limpiarColeccion() {
        mongoTemplate.getDb().getCollection("boletos").deleteMany(new org.bson.Document());
        mongoTemplate.getDb().getCollection("reservas").deleteMany(new org.bson.Document());
    }

    @Test
    void guardarSinDuplicarNoCreaDosBoletosParaElMismoAsientoAunqueSeLlameDosVeces() {
        String reservaId = "reserva-1";
        List<BoletoDigital> boletos = List.of(
                BoletoDigital.sinFirma("b1", reservaId, "a1", "COD1", LocalDateTime.now()));

        adapter.guardarSinDuplicar(boletos);
        adapter.guardarSinDuplicar(boletos); // mismo webhook llega dos veces

        assertThat(adapter.porReserva(reservaId)).hasSize(1);
    }

    @Test
    void emiteExactamenteUnBoletoPorCadaAsientoDeLaReserva() {
        String reservaId = "reserva-2";
        List<BoletoDigital> boletos = List.of(
                BoletoDigital.sinFirma("b1", reservaId, "a1", "COD1", LocalDateTime.now()),
                BoletoDigital.sinFirma("b2", reservaId, "a2", "COD2", LocalDateTime.now()));

        adapter.guardarSinDuplicar(boletos);

        assertThat(adapter.porReserva(reservaId)).hasSize(2);
    }

    @Test
    void permiteBuscarUnBoletoPorSuIdentificadorDeNegocio() {
        BoletoDigital boleto = BoletoDigital.sinFirma("boleto-xyz", "reserva-3", "a1", "COD1", LocalDateTime.now());
        adapter.guardarSinDuplicar(List.of(boleto));

        assertThat(adapter.buscarPorId("boleto-xyz")).isPresent();
        assertThat(adapter.buscarPorId("no-existe")).isEmpty();
    }
}
