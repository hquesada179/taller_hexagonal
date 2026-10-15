package com.stagepass.live.adapter.out.firma;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HmacAdapterTest {

    private final HmacAdapter firma = new HmacAdapter("clave-maestra-de-prueba");

    @Test
    void generaUnaFirmaHexadecimalDeSha256() {
        String firmaCalculada = firma.firmar("datos-del-boleto", "evt-1");

        assertThat(firmaCalculada).hasSize(64); // 32 bytes en hexadecimal
        assertThat(firmaCalculada).matches("[0-9a-f]{64}");
    }

    @Test
    void laFirmaEsDeterministaParaLosMismosDatosYEvento() {
        String firma1 = firma.firmar("datos-del-boleto", "evt-1");
        String firma2 = firma.firmar("datos-del-boleto", "evt-1");

        assertThat(firma1).isEqualTo(firma2);
    }

    @Test
    void laFirmaVariaSegunElEvento_porqueLaClaveSeDerivaPorEvento() {
        String firmaEvento1 = firma.firmar("datos-del-boleto", "evt-1");
        String firmaEvento2 = firma.firmar("datos-del-boleto", "evt-2");

        assertThat(firmaEvento1).isNotEqualTo(firmaEvento2);
    }

    @Test
    void verificaCorrectamenteUnaFirmaValida() {
        String firmaCalculada = firma.firmar("datos-del-boleto", "evt-1");

        assertThat(firma.verificar("datos-del-boleto", firmaCalculada, "evt-1")).isTrue();
    }

    @Test
    void detectaDatosAlteradosComoFirmaInvalida() {
        String firmaCalculada = firma.firmar("datos-originales", "evt-1");

        assertThat(firma.verificar("datos-alterados", firmaCalculada, "evt-1")).isFalse();
    }
}
