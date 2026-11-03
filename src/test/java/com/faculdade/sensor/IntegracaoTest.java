package com.faculdade.sensor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Os quatro padrões juntos, no mesmo cenário da demonstração. */
class IntegracaoTest {

    private final CentralMonitoramento central = CentralMonitoramento.getInstancia();
    private Sensor vibracao;

    @BeforeEach
    void montarPlanta() {
        central.limpar();
        new FabricaTemperatura().criar("T-01", FonteLeitura.sequencia(70, 78, 85, 92, 95));
        new FabricaPressao().criar("P-01", FonteLeitura.sequencia(600, 650, 900, 1000, 1000));
        vibracao = new FabricaVibracao().criar("V-01", FonteLeitura.sequencia(2.0, 2.2, 9.0, 2.1, 9.5));
    }

    private static List<Nivel> niveis(List<Medicao> ciclo) {
        return ciclo.stream().map(Medicao::nivel).toList();
    }

    @Test
    void ciclosDevemAplicarACadaSensorSuaEstrategiaPadrao() {
        // ordem: T-01 (limite fixo), P-01 (taxa de variação), V-01 (média móvel)
        assertEquals(List.of(Nivel.NORMAL, Nivel.NORMAL, Nivel.NORMAL), niveis(central.medirTodos()));
        assertEquals(List.of(Nivel.NORMAL, Nivel.NORMAL, Nivel.NORMAL), niveis(central.medirTodos()));
        assertEquals(List.of(Nivel.ALERTA, Nivel.ALERTA, Nivel.NORMAL), niveis(central.medirTodos()));
        assertEquals(List.of(Nivel.CRITICO, Nivel.NORMAL, Nivel.NORMAL), niveis(central.medirTodos()));
    }

    @Test
    void medicoesDevemTrazerOsValoresJaConvertidos() {
        List<Medicao> ciclo = central.medirTodos();

        assertEquals(new Medicao("T-01", "TEMPERATURA", 70.0, "°C", Nivel.NORMAL), ciclo.get(0));
        assertEquals(new Medicao("P-01", "PRESSAO", 6.0, "bar", Nivel.NORMAL), ciclo.get(1));
        assertEquals(new Medicao("V-01", "VIBRACAO", 2.0, "mm/s", Nivel.NORMAL), ciclo.get(2));
    }

    @Test
    void trocarAEstrategiaDeUmSensorNaoDeveAfetarOsDemais() {
        for (int i = 0; i < 4; i++) {
            central.medirTodos();
        }
        // Sem a troca, a média móvel de V-01 no 5º ciclo seria ALERTA.
        List<Double> comQuinta = new java.util.ArrayList<>(vibracao.getHistorico());
        comQuinta.add(9.5);
        assertEquals(Nivel.ALERTA, new MediaMovel(3, 5, 8).avaliar(comQuinta));

        vibracao.trocarEstrategia(new LimiteFixo(5, 8));
        List<Medicao> quinto = central.medirTodos();

        assertEquals(List.of(Nivel.CRITICO, Nivel.NORMAL, Nivel.CRITICO), niveis(quinto));
        assertInstanceOf(LimiteFixo.class, central.buscar("V-01").orElseThrow().getEstrategia());
        assertInstanceOf(TaxaVariacao.class, central.buscar("P-01").orElseThrow().getEstrategia());
    }

    @Test
    void sensorBuscadoNaCentralDeveSerOMesmoObjetoCriadoPelaFabrica() {
        assertSame(vibracao, central.buscar("V-01").orElseThrow());
    }

    @Test
    void leituraImpossivelEmUmSensorDevePropagarEMantemOsOutrosHistoricosCoerentes() {
        central.limpar();
        Sensor bom = new FabricaTemperatura().criar("T-01", FonteLeitura.sequencia(70, 71));
        new FabricaVibracao().criar("V-01", FonteLeitura.sequencia(2.0, -5.0));   // -5 mm/s é impossível

        central.medirTodos();
        assertThrows(IllegalStateException.class, central::medirTodos);

        // T-01 (registrado antes) já tinha medido no 2º ciclo; V-01 não guardou o valor inválido.
        assertEquals(List.of(70.0, 71.0), bom.getHistorico());
        assertEquals(List.of(2.0), central.buscar("V-01").orElseThrow().getHistorico());
    }
}
