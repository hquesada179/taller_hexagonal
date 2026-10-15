package com.stagepass.live.adapter.out.mongo;

import com.mongodb.client.MongoClients;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integracion REAL contra MongoDB (via Testcontainers): demuestra
 * que, incluso con varios hilos intentando bloquear el MISMO asiento de la
 * MISMA funcion al mismo tiempo, solo uno de ellos puede tener exito. Esta
 * es la evidencia tecnica de que nunca se vende dos veces el mismo asiento.
 */
@Testcontainers
class MongoOcupacionAdapterIT {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    static MongoTemplate mongoTemplate;
    static MongoOcupacionAdapter adapter;

    @BeforeAll
    static void conectar() {
        mongoTemplate = new MongoTemplate(MongoClients.create(MONGO.getReplicaSetUrl()), "stagepass-test");
        adapter = new MongoOcupacionAdapter(mongoTemplate);
    }

    @AfterAll
    static void cerrar() {
        mongoTemplate.getDb().drop();
    }

    @BeforeEach
    void limpiarColeccion() {
        mongoTemplate.getDb().getCollection("disponibilidad_asientos").deleteMany(new org.bson.Document());
    }

    @Test
    void soloUnaDeVariasSolicitudesConcurrentesLograBloquearElMismoAsiento() throws InterruptedException {
        String eventoId = "evt-concurrencia";
        String asientoId = "asi-unico";
        int intentos = 20;

        ExecutorService pool = Executors.newFixedThreadPool(intentos);
        CountDownLatch salida = new CountDownLatch(intentos);
        AtomicInteger exitos = new AtomicInteger(0);

        for (int i = 0; i < intentos; i++) {
            String reservaId = "reserva-" + i;
            pool.submit(() -> {
                try {
                    boolean logrado = adapter.reservarAtomico(eventoId, List.of(asientoId), reservaId,
                            LocalDateTime.now().plusMinutes(10), "cliente-" + reservaId);
                    if (logrado) {
                        exitos.incrementAndGet();
                    }
                } finally {
                    salida.countDown();
                }
            });
        }

        salida.await(30, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(exitos.get()).isEqualTo(1);
    }

    @Test
    void liberarPermiteQueOtraReservaBloqueeElAsientoLuego() {
        String eventoId = "evt-liberacion";
        String asientoId = "asi-1";

        boolean primerBloqueo = adapter.reservarAtomico(eventoId, List.of(asientoId), "r1",
                LocalDateTime.now().plusMinutes(10), "cli1");
        assertThat(primerBloqueo).isTrue();

        boolean segundoIntentoFallido = adapter.reservarAtomico(eventoId, List.of(asientoId), "r2",
                LocalDateTime.now().plusMinutes(10), "cli2");
        assertThat(segundoIntentoFallido).isFalse();

        adapter.liberar("r1");

        boolean bloqueoTrasLiberar = adapter.reservarAtomico(eventoId, List.of(asientoId), "r3",
                LocalDateTime.now().plusMinutes(10), "cli3");
        assertThat(bloqueoTrasLiberar).isTrue();
    }

    @Test
    void confirmarVentaSoloMarcaVendidosLosAsientosBloqueadosPorEsaReserva() {
        String eventoId = "evt-venta";
        adapter.reservarAtomico(eventoId, List.of("a1", "a2"), "r1", LocalDateTime.now().plusMinutes(10), "cli1");

        adapter.confirmarVenta("r1");

        boolean puedeBloquearseDeNuevo = adapter.reservarAtomico(eventoId, List.of("a1"), "r2",
                LocalDateTime.now().plusMinutes(10), "cli2");
        assertThat(puedeBloquearseDeNuevo).isFalse(); // ya esta VENDIDO, no DISPONIBLE
    }
}
