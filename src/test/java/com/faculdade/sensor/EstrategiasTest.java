package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** As três estratégias usadas na integração, isoladas: histórico -> nível. */
class EstrategiasTest {

    @Test
    void limiteFixoDeveClassificarPelaUltimaLeituraIncluindoOsLimites() {
        EstrategiaAvaliacao e = new LimiteFixo(80, 90);

        assertEquals(Nivel.NORMAL, e.avaliar(List.of(79.9)));
        assertEquals(Nivel.ALERTA, e.avaliar(List.of(80.0)));
        assertEquals(Nivel.CRITICO, e.avaliar(List.of(90.0)));
        assertEquals(Nivel.NORMAL, e.avaliar(List.of(120.0, 50.0)));   // só a última conta
    }

    @Test
    void taxaDeveUsarOMóduloDaVariacaoEIgnorarOValorAbsoluto() {
        EstrategiaAvaliacao e = new TaxaVariacao(1.5, 3.0);

        assertEquals(Nivel.NORMAL, e.avaliar(List.of(50.0)));          // sem anterior
        assertEquals(Nivel.NORMAL, e.avaliar(List.of(50.0, 50.5)));
        assertEquals(Nivel.ALERTA, e.avaliar(List.of(50.0, 51.5)));
        assertEquals(Nivel.CRITICO, e.avaliar(List.of(50.0, 47.0)));   // queda também conta
    }

    @Test
    void mediaDeveUsarSomenteAJanelaEAbsorverPicos() {
        EstrategiaAvaliacao e = new MediaMovel(3, 5, 8);

        assertEquals(Nivel.NORMAL, e.avaliar(List.of(2.0, 2.2, 9.0)));            // média 4,4
        assertEquals(Nivel.ALERTA, e.avaliar(List.of(100.0, 4.0, 5.0, 6.0)));     // 100 fora da janela
        assertEquals(Nivel.CRITICO, e.avaliar(List.of(9.0)));                     // histórico menor que a janela
    }

    @Test
    void construtoresDevemRejeitarParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new LimiteFixo(90, 80));
        assertThrows(IllegalArgumentException.class, () -> new TaxaVariacao(0, 3));
        assertThrows(IllegalArgumentException.class, () -> new TaxaVariacao(3, 1.5));
        assertThrows(IllegalArgumentException.class, () -> new MediaMovel(0, 5, 8));
        assertThrows(IllegalArgumentException.class, () -> new MediaMovel(3, 8, 5));
    }
}
