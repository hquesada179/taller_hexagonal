package com.stagepass.live.adapter.out.mongo;

import com.stagepass.live.adapter.out.mongo.document.OcupacionDocument;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prueba unitaria (sin Docker) de la logica de rollback de MongoOcupacionAdapter
 * ante fallos inesperados a mitad del bloqueo de varios asientos.
 */
class MongoOcupacionAdapterTest {

    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final MongoOcupacionAdapter adapter = new MongoOcupacionAdapter(mongoTemplate);

    @Test
    void liberaLosAsientosYaBloqueadosSiUnErrorInesperadoInterrumpeElBloqueoDeVariosAsientos() {
        // Regresion: al bloquear ["a1", "a2"], el primer asiento (a1) se bloquea con
        // exito y el segundo (a2) falla con un error INESPERADO de MongoDB (no un
        // "ya esta tomado"). El asiento a1, ya bloqueado en este mismo intento, no
        // debe quedar bloqueado para siempre: reservarAtomico debe liberarlo antes
        // de propagar la excepcion.
        when(mongoTemplate.insert(any(OcupacionDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0))
                .thenThrow(new DataAccessResourceFailureException("fallo transitorio simulado de MongoDB"));

        assertThatThrownBy(() -> adapter.reservarAtomico("evt1", List.of("a1", "a2"), "r1",
                LocalDateTime.now().plusMinutes(10), "cli1"))
                .isInstanceOf(DataAccessResourceFailureException.class);

        verify(mongoTemplate).updateMulti(any(Query.class), any(Update.class), eq(OcupacionDocument.class));
    }
}
