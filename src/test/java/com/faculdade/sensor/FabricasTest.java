package com.faculdade.sensor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FabricasTest {

    private final CentralMonitoramento central = CentralMonitoramento.getInstancia();

    @BeforeEach
    void limparCentral() {
        central.limpar();
    }

    @Test
    void cadaFabricaDeveCriarOTipoCerto() {
        Sensor t = new FabricaTemperatura().criar("T-01", FonteLeitura.sequencia(1));
        Sensor p = new FabricaPressao().criar("P-01", FonteLeitura.sequencia(1));
        Sensor v = new FabricaVibracao().criar("V-01", FonteLeitura.sequencia(1));

        assertInstanceOf(SensorTemperatura.class, t);
        assertInstanceOf(SensorPressao.class, p);
        assertInstanceOf(SensorVibracao.class, v);
    }

    @Test
    void fabricaDeTemperaturaDeveUsarLimiteFixo() {
        Sensor t = new FabricaTemperatura().criar("T-01", FonteLeitura.sequencia(79.0, 85.0, 95.0));

        assertEquals(Nivel.NORMAL, t.medir().nivel());
        assertEquals(Nivel.ALERTA, t.medir().nivel());
        assertEquals(Nivel.CRITICO, t.medir().nivel());
        assertInstanceOf(LimiteFixo.class, t.getEstrategia());
    }

    @Test
    void fabricaDePressaoDeveUsarTaxaDeVariacao() {
        Sensor p = new FabricaPressao().criar("P-01", FonteLeitura.sequencia(600.0, 900.0));

        assertEquals(Nivel.NORMAL, p.medir().nivel());
        assertEquals(Nivel.CRITICO, p.medir().nivel());   // salto de 6 para 9 bar
        assertInstanceOf(TaxaVariacao.class, p.getEstrategia());
    }

    @Test
    void fabricaDeVibracaoDeveUsarMediaMovelQueAbsorvePicos() {
        Sensor v = new FabricaVibracao().criar("V-01", FonteLeitura.sequencia(2.0, 2.2, 9.0));

        v.medir();
        v.medir();
        assertEquals(Nivel.NORMAL, v.medir().nivel());   // pico de 9, média 4,4
        assertInstanceOf(MediaMovel.class, v.getEstrategia());
    }

    @Test
    void criarDeveRegistrarOSensorNaCentralUnica() {
        Sensor t = new FabricaTemperatura().criar("T-01", FonteLeitura.sequencia(1));

        assertSame(t, central.buscar("T-01").orElseThrow());
        assertEquals(1, central.quantidade());
    }

    @Test
    void idRepetidoDeveSerRecusadoSemSubstituirOSensorOriginal() {
        FabricaSensor fabrica = new FabricaTemperatura();
        Sensor primeiro = fabrica.criar("T-01", FonteLeitura.sequencia(1));

        assertThrows(IllegalStateException.class, () -> fabrica.criar("T-01", FonteLeitura.sequencia(2)));

        assertEquals(1, central.quantidade());
        assertSame(primeiro, central.buscar("T-01").orElseThrow());
    }

    @Test
    void falhaNaCriacaoNaoDeveRegistrarNada() {
        FabricaSensor fabrica = new FabricaTemperatura();

        assertThrows(NullPointerException.class, () -> fabrica.criar(null, FonteLeitura.sequencia(1)));
        assertThrows(NullPointerException.class, () -> fabrica.criar("T-01", null));

        assertEquals(0, central.quantidade());
    }

    @Test
    void clienteDeveUsarTodasAsFabricasPeloTipoAbstrato() {
        List<FabricaSensor> fabricas = List.of(new FabricaTemperatura(), new FabricaPressao(), new FabricaVibracao());
        int n = 0;
        for (FabricaSensor f : fabricas) {
            f.criar("S-" + n++, FonteLeitura.sequencia(10));
        }

        assertEquals(3, central.quantidade());
        List<Medicao> medicoes = central.medirTodos();
        assertEquals(List.of("TEMPERATURA", "PRESSAO", "VIBRACAO"),
                medicoes.stream().map(Medicao::tipo).toList());
    }

    @Test
    void novaFabricaPodeSerAdicionadaSemAlterarClassesExistentes() {

        FabricaSensor fabricaRigida = new FabricaSensor() {
            @Override
            protected Sensor criarSensor(String id, FonteLeitura fonte) {
                return new SensorTemperatura(id, fonte, new LimiteFixo(50, 60));
            }
        };

        Sensor s = fabricaRigida.criar("T-RIGIDO", FonteLeitura.sequencia(55.0));

        assertEquals(Nivel.ALERTA, s.medir().nivel());
        assertTrue(central.buscar("T-RIGIDO").isPresent());
    }

    @Test
    void estruturaDoFactoryMethodDeveSerProtegida() throws NoSuchMethodException {
        var factory = FabricaSensor.class.getDeclaredMethod("criarSensor", String.class, FonteLeitura.class);
        var criar = FabricaSensor.class.getMethod("criar", String.class, FonteLeitura.class);

        assertTrue(Modifier.isAbstract(factory.getModifiers()));
        assertTrue(Modifier.isProtected(factory.getModifiers()));
        assertTrue(Modifier.isFinal(criar.getModifiers()));
    }
}
