package com.stagepass.live.domain.policy;

import com.stagepass.live.domain.exception.LimiteBoletosExcedidoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoliticaLimiteTest {

    private final PoliticaLimite politica = new PoliticaLimite();

    @Test
    void permiteExactamenteSeisBoletosEnTotal() {
        assertThatCode(() -> politica.validar(2, 1, 3)).doesNotThrowAnyException();
    }

    @Test
    void rechazaCompraQueSuperaSeisBoletosAcumulados() {
        assertThatThrownBy(() -> politica.validar(2, 2, 3))
                .isInstanceOf(LimiteBoletosExcedidoException.class);
    }

    @Test
    void rechazaCuandoSoloLaNuevaSolicitudYaSuperaElLimite() {
        assertThatThrownBy(() -> politica.validar(0, 0, 7))
                .isInstanceOf(LimiteBoletosExcedidoException.class);
    }

    @Test
    void respetaUnLimitePersonalizado() {
        PoliticaLimite limitePersonalizado = new PoliticaLimite(4);
        assertThatThrownBy(() -> limitePersonalizado.validar(0, 0, 5))
                .isInstanceOf(LimiteBoletosExcedidoException.class);
    }
}
