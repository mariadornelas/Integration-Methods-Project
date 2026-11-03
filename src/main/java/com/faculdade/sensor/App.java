package com.faculdade.sensor;

import java.util.List;

public class App {

    public static void main(String[] args) {
        CentralMonitoramento central = CentralMonitoramento.getInstancia();
        central.limpar();

        FabricaSensor temperatura = new FabricaTemperatura();
        FabricaSensor pressao = new FabricaPressao();
        FabricaSensor vibracao = new FabricaVibracao();

        temperatura.criar("T-01", FonteLeitura.sequencia(70, 78, 85, 92, 95));
        pressao.criar("P-01", FonteLeitura.sequencia(600, 650, 900, 1000, 1000));   // kPa
        Sensor v1 = vibracao.criar("V-01", FonteLeitura.sequencia(2.0, 2.2, 9.0, 2.1, 9.5));

        System.out.println("Sensores registrados na central: " + central.quantidade());
        System.out.println("Mesma instância em outro ponto do código? "
                + (central == CentralMonitoramento.getInstancia()));
        System.out.println();

        System.out.println("=== Ciclos de medição (Template Method + Strategy) ===");
        for (int ciclo = 1; ciclo <= 4; ciclo++) {
            imprimir(ciclo, central.medirTodos());
        }

        System.out.println();
        System.out.println("=== Strategy em tempo de execução: V-01 passa de média móvel para limite fixo ===");
        System.out.println("Histórico de V-01: " + v1.getHistorico());
        System.out.println("Com a média móvel o 5º ciclo seria: "
                + new MediaMovel(3, 5, 8).avaliar(simular(v1, 9.5)));
        v1.trocarEstrategia(new LimiteFixo(5, 8));
        imprimir(5, central.medirTodos());

        System.out.println();
        System.out.println("=== Id repetido é recusado pela central ===");
        try {
            temperatura.criar("T-01", FonteLeitura.sequencia(1));
        } catch (IllegalStateException e) {
            System.out.println("Recusado: " + e.getMessage());
        }
    }

    private static List<Double> simular(Sensor sensor, double proxima) {
        java.util.List<Double> lista = new java.util.ArrayList<>(sensor.getHistorico());
        lista.add(proxima);
        return lista;
    }

    private static void imprimir(int ciclo, List<Medicao> medicoes) {
        System.out.println("Ciclo " + ciclo + ":");
        for (Medicao m : medicoes) {
            System.out.printf(java.util.Locale.ROOT, "  %-5s %-12s %7.2f %-5s -> %s%n",
                    m.sensorId(), m.tipo(), m.valor(), m.unidade(), m.nivel());
        }
    }
}
