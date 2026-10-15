package com.stagepass.live.adapter.out.mongo;

import com.mongodb.client.MongoClients;
import com.stagepass.live.domain.enums.EstadoReserva;
import com.stagepass.live.domain.model.Reserva;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integracion REAL contra MongoDB para las transiciones atomicas
 * de estado de una reserva (aprobarSiVigente / expirarSiPendiente), que son
 * la base de la idempotencia ante webhooks duplicados y de la expiracion
 * automatica de bloqueos vencidos.
 */
@Testcontainers
class MongoReservaAdapterIT {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    static MongoTemplate mongoTemplate;
    static MongoReservaAdapter adapter;

    private static final LocalDateTime CREADA = LocalDateTime.of(2026, 10, 15, 10, 0, 0);
    private static final LocalDateTime EXPIRA = CREADA.plusMinutes(10);

    @BeforeAll
    static void conectar() {
        mongoTemplate = new MongoTemplate(MongoClients.create(MONGO.getReplicaSetUrl()), "stagepass-test");
        adapter = new MongoReservaAdapter(mongoTemplate);
    }

    @AfterAll
    static void cerrar() {
        mongoTemplate.getDb().drop();
    }

    @BeforeEach
    void limpiarColeccion() {
        mongoTemplate.getDb().getCollection("reservas").deleteMany(new org.bson.Document());
    }

    private Reserva nuevaReservaPendiente(String id) {
        return new Reserva(id, "evt1", "cli1", List.of("a1"),
                new BigDecimal("100000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO,
                new BigDecimal("110000.00"), EXPIRA, EstadoReserva.PENDIENTE_PAGO);
    }

    @Test
    void aprobarSiVigenteSoloTransicionaUnaVezAunConLlamadasRepetidas() {
        adapter.guardar(nuevaReservaPendiente("r1"));

        boolean primeraVez = adapter.aprobarSiVigente("r1", CREADA.plusMinutes(5));
        boolean segundaVez = adapter.aprobarSiVigente("r1", CREADA.plusMinutes(5));

        assertThat(primeraVez).isTrue();
        assertThat(segundaVez).isFalse(); // ya no esta PENDIENTE_PAGO: evita doble aprobacion

        assertThat(adapter.buscar("r1")).get().extracting(Reserva::getEstado).isEqualTo(EstadoReserva.APROBADA);
    }

    @Test
    void noAprueabaUnaReservaCuyoBloqueoYaVencioEnElInstanteIndicado() {
        adapter.guardar(nuevaReservaPendiente("r2"));

        boolean aprobada = adapter.aprobarSiVigente("r2", EXPIRA.plusSeconds(1));

        assertThat(aprobada).isFalse();
        assertThat(adapter.buscar("r2")).get().extracting(Reserva::getEstado).isEqualTo(EstadoReserva.PENDIENTE_PAGO);
    }

    @Test
    void expirarSiPendienteLiberaSoloLasQueSiguenPendientesYVencidas() {
        adapter.guardar(nuevaReservaPendiente("r3"));

        boolean expirada = adapter.expirarSiPendiente("r3", EXPIRA);

        assertThat(expirada).isTrue();
        assertThat(adapter.buscar("r3")).get().extracting(Reserva::getEstado).isEqualTo(EstadoReserva.EXPIRADA);
    }

    @Test
    void vencidasDevuelveSoloLasReservasPendientesCuyoBloqueoYaPaso() {
        adapter.guardar(nuevaReservaPendiente("r4"));

        List<Reserva> vencidas = adapter.vencidas(EXPIRA.plusSeconds(1));

        assertThat(vencidas).extracting(Reserva::getId).contains("r4");
    }

    @Test
    void contarReservadosCuentaSoloPendientesDelMismoClienteYEvento() {
        adapter.guardar(nuevaReservaPendiente("r5"));

        assertThat(adapter.contarReservados("cli1", "evt1")).isEqualTo(1);
        assertThat(adapter.contarReservados("otro-cliente", "evt1")).isZero();
    }

    @Test
    void contarReservadosSumaLosAsientosDeCadaReservaYNoSoloElNumeroDeDocumentos() {
        // Regresion: una sola reserva PENDIENTE_PAGO con varios asientos debe
        // contar todos sus asientos, no "1" por documento. Dos reservas de 3
        // asientos cada una deben sumar 6, no 2.
        Reserva reservaDeTresAsientos1 = new Reserva("r6", "evt1", "cli1", List.of("a1", "a2", "a3"),
                new BigDecimal("300000.00"), new BigDecimal("24000.00"), new BigDecimal("15000.00"),
                new BigDecimal("309000.00"), EXPIRA, EstadoReserva.PENDIENTE_PAGO);
        Reserva reservaDeTresAsientos2 = new Reserva("r7", "evt1", "cli1", List.of("a4", "a5", "a6"),
                new BigDecimal("300000.00"), new BigDecimal("24000.00"), new BigDecimal("15000.00"),
                new BigDecimal("309000.00"), EXPIRA, EstadoReserva.PENDIENTE_PAGO);

        adapter.guardar(reservaDeTresAsientos1);
        adapter.guardar(reservaDeTresAsientos2);

        assertThat(adapter.contarReservados("cli1", "evt1")).isEqualTo(6);
    }
}
