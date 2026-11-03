package com.faculdade.sensor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Singleton: instância única, construtor fechado, acesso seguro entre threads. */
class CentralMonitoramentoTest {

    private final CentralMonitoramento central = CentralMonitoramento.getInstancia();

    @BeforeEach
    void limparCentral() {
        central.limpar();
    }

    private static Sensor sensor(String id, double... valores) {
        return new SensorTemperatura(id, FonteLeitura.sequencia(valores), l -> Nivel.NORMAL);
    }

    // ---- Singleton ----

    @Test
    void getInstanciaDeveSempreDevolverOMesmoObjeto() {
        assertSame(CentralMonitoramento.getInstancia(), CentralMonitoramento.getInstancia());
    }

    @Test
    void construtorDeveSerPrivadoEAClasseFinal() throws NoSuchMethodException {
        Constructor<CentralMonitoramento> c = CentralMonitoramento.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(c.getModifiers()));
        assertTrue(Modifier.isFinal(CentralMonitoramento.class.getModifiers()));
        assertEquals(0, CentralMonitoramento.class.getConstructors().length);
    }

    @Test
    void todasAsThreadsDevemReceberAMesmaInstancia() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<CentralMonitoramento>> futuros = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                futuros.add(pool.submit(CentralMonitoramento::getInstancia));
            }
            Set<CentralMonitoramento> distintas = new HashSet<>();
            for (Future<CentralMonitoramento> f : futuros) {
                distintas.add(f.get(5, TimeUnit.SECONDS));
            }
            assertEquals(1, distintas.size());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void registrosConcorrentesNaoDevemSeperder() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> futuros = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                final String id = "S-" + i;
                futuros.add(pool.submit(() -> central.registrar(sensor(id, 1))));
            }
            for (Future<?> f : futuros) {
                f.get(5, TimeUnit.SECONDS);
            }
            assertEquals(100, central.quantidade());
        } finally {
            pool.shutdownNow();
        }
    }

    // ---- registro ----

    @Test
    void deveRegistrarEBuscarPorId() {
        Sensor s = sensor("T-01", 1);

        central.registrar(s);

        assertSame(s, central.buscar("T-01").orElseThrow());
        assertTrue(central.buscar("INEXISTENTE").isEmpty());
    }

    @Test
    void deveRecusarIdRepetidoENulo() {
        central.registrar(sensor("T-01", 1));

        assertThrows(IllegalStateException.class, () -> central.registrar(sensor("T-01", 2)));
        assertThrows(NullPointerException.class, () -> central.registrar(null));
        assertEquals(1, central.quantidade());
    }

    @Test
    void medirTodosDeveRespeitarAOrdemDeRegistro() {
        central.registrar(sensor("B", 1));
        central.registrar(sensor("A", 2));
        central.registrar(sensor("C", 3));

        List<String> ids = central.medirTodos().stream().map(Medicao::sensorId).toList();

        assertEquals(List.of("B", "A", "C"), ids);
    }

    @Test
    void cadaCicloDeveMedirCadaSensorUmaVez() {
        Sensor s = sensor("T-01", 10, 20);
        central.registrar(s);

        central.medirTodos();
        central.medirTodos();

        assertEquals(List.of(10.0, 20.0), s.getHistorico());
    }

    @Test
    void medirTodosSemSensoresDeveDevolverListaVazia() {
        assertEquals(Collections.emptyList(), central.medirTodos());
    }

    @Test
    void limparDeveEsvaziarSemTrocarAInstancia() {
        central.registrar(sensor("T-01", 1));

        central.limpar();

        assertEquals(0, central.quantidade());
        assertSame(central, CentralMonitoramento.getInstancia());
    }
}
