package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SensorTemplateTest {

    private static final EstrategiaAvaliacao SEMPRE_NORMAL = l -> Nivel.NORMAL;

    @Test
    void medirDeveMontarAMedicaoCompleta() {
        Sensor s = new SensorTemperatura("T-01", FonteLeitura.sequencia(85.0), new LimiteFixo(80, 90));

        Medicao m = s.medir();

        assertEquals(new Medicao("T-01", "TEMPERATURA", 85.0, "°C", Nivel.ALERTA), m);
    }

    @Test
    void hookConverterDeveTransformarOValorBrutoEmUnidadeDoSensor() {
        Sensor s = new SensorPressao("P-01", FonteLeitura.sequencia(650.0), SEMPRE_NORMAL);

        Medicao m = s.medir();

        assertEquals(6.5, m.valor(), 1e-9);
        assertEquals("bar", m.unidade());
        assertEquals(List.of(6.5), s.getHistorico());
    }

    @Test
    void sensoresSemConversaoDevemManterOValorBruto() {
        assertEquals(2.5, new SensorVibracao("V-01", FonteLeitura.sequencia(2.5), SEMPRE_NORMAL).medir().valor());
        assertEquals(40.0, new SensorTemperatura("T-01", FonteLeitura.sequencia(40.0), SEMPRE_NORMAL).medir().valor());
    }

    @Test
    void hookValidarDeveRejeitarLeiturasFisicamenteImpossiveis() {
        assertThrows(IllegalStateException.class,
                () -> new SensorTemperatura("T", FonteLeitura.sequencia(1200.0), SEMPRE_NORMAL).medir());
        assertThrows(IllegalStateException.class,
                () -> new SensorTemperatura("T", FonteLeitura.sequencia(-60.0), SEMPRE_NORMAL).medir());
        assertThrows(IllegalStateException.class,
                () -> new SensorPressao("P", FonteLeitura.sequencia(-10.0), SEMPRE_NORMAL).medir());
        assertThrows(IllegalStateException.class,
                () -> new SensorVibracao("V", FonteLeitura.sequencia(-1.0), SEMPRE_NORMAL).medir());
        assertThrows(IllegalStateException.class,
                () -> new SensorVibracao("V", FonteLeitura.sequencia(Double.NaN), SEMPRE_NORMAL).medir());
        assertThrows(IllegalStateException.class,
                () -> new SensorVibracao("V", FonteLeitura.sequencia(Double.POSITIVE_INFINITY), SEMPRE_NORMAL).medir());
    }

    @Test
    void valoresNosLimitesDaFaixaFisicaDevemSerAceitos() {
        assertDoesNotThrow(() -> new SensorTemperatura("T", FonteLeitura.sequencia(-50.0), SEMPRE_NORMAL).medir());
        assertDoesNotThrow(() -> new SensorTemperatura("T", FonteLeitura.sequencia(1000.0), SEMPRE_NORMAL).medir());
    }

    @Test
    void leituraInvalidaNaoDeveEntrarNoHistoricoNemChegarAEstrategia() {
        List<Integer> tamanhosVistos = new ArrayList<>();
        EstrategiaAvaliacao espia = l -> {
            tamanhosVistos.add(l.size());
            return Nivel.NORMAL;
        };
        Sensor s = new SensorTemperatura("T-01", FonteLeitura.sequencia(70.0, 5000.0, 72.0), espia);

        s.medir();
        assertThrows(IllegalStateException.class, s::medir);
        s.medir();

        assertEquals(List.of(70.0, 72.0), s.getHistorico());
        assertEquals(List.of(1, 2), tamanhosVistos);
    }

    @Test
    void ordemDoEsqueletoDeveSerLerConverterValidarAvaliar() {
        List<String> passos = new ArrayList<>();
        FonteLeitura fonte = () -> {
            passos.add("ler");
            return 50.0;
        };
        EstrategiaAvaliacao estrategia = l -> {
            passos.add("avaliar");
            return Nivel.NORMAL;
        };
        Sensor espiao = new Sensor("X-01", fonte, estrategia) {
            @Override
            protected double converter(double bruto) {
                passos.add("converter");
                return super.converter(bruto);
            }

            @Override
            protected void validar(double valor) {
                passos.add("validar");
                super.validar(valor);
            }

            @Override
            protected String tipo() {
                return "TESTE";
            }

            @Override
            protected String unidade() {
                return "u";
            }

            @Override
            protected double minimoFisico() {
                return 0;
            }

            @Override
            protected double maximoFisico() {
                return 100;
            }
        };

        espiao.medir();

        assertEquals(List.of("ler", "converter", "validar", "avaliar"), passos);
    }

    @Test
    void medirDeveSerFinalParaProtegerOEsqueleto() throws NoSuchMethodException {
        assertTrue(Modifier.isFinal(Sensor.class.getMethod("medir").getModifiers()));
    }

    @Test
    void trocarEstrategiaDeveMudarOVeredictoDasProximasMedicoes() {
        Sensor s = new SensorTemperatura("T-01", FonteLeitura.sequencia(85.0, 85.0), new LimiteFixo(80, 90));
        assertEquals(Nivel.ALERTA, s.medir().nivel());

        s.trocarEstrategia(l -> Nivel.CRITICO);

        assertEquals(Nivel.CRITICO, s.medir().nivel());
    }

    @Test
    void estrategiaNaoDevePoderAlterarOHistoricoDoSensor() {
        Sensor s = new SensorTemperatura("T-01", FonteLeitura.sequencia(70.0), l -> {
            l.clear();
            return Nivel.NORMAL;
        });

        assertThrows(UnsupportedOperationException.class, s::medir);
        assertEquals(List.of(70.0), s.getHistorico());
    }

    @Test
    void historicoExpostoDeveSerSomenteLeitura() {
        Sensor s = new SensorTemperatura("T-01", FonteLeitura.sequencia(70.0), SEMPRE_NORMAL);
        s.medir();

        assertThrows(UnsupportedOperationException.class, () -> s.getHistorico().add(1.0));
    }

    @Test
    void construtorDeveRejeitarArgumentosInvalidos() {
        FonteLeitura f = FonteLeitura.sequencia(1);
        assertThrows(NullPointerException.class, () -> new SensorTemperatura(null, f, SEMPRE_NORMAL));
        assertThrows(IllegalArgumentException.class, () -> new SensorTemperatura("  ", f, SEMPRE_NORMAL));
        assertThrows(NullPointerException.class, () -> new SensorTemperatura("T", null, SEMPRE_NORMAL));
        assertThrows(NullPointerException.class, () -> new SensorTemperatura("T", f, null));
        Sensor s = new SensorTemperatura("T", f, SEMPRE_NORMAL);
        assertThrows(NullPointerException.class, () -> s.trocarEstrategia(null));
    }

    @Test
    void fonteSimuladaDeveFalharQuandoAcabarEManterIndependenciaDoArrayOriginal() {
        double[] original = {1.0, 2.0};
        FonteLeitura f = FonteLeitura.sequencia(original);
        original[0] = 99.0;

        assertEquals(1.0, f.ler());
        assertEquals(2.0, f.ler());
        assertThrows(IllegalStateException.class, f::ler);
    }
}
