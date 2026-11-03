package com.faculdade.sensor;

import java.util.List;

public class MediaMovel implements EstrategiaAvaliacao {

    private final int janela;
    private final double limiteAlerta;
    private final double limiteCritico;

    public MediaMovel(int janela, double limiteAlerta, double limiteCritico) {
        if (janela < 1) {
            throw new IllegalArgumentException("a janela deve ter ao menos 1 leitura");
        }
        if (!(limiteAlerta < limiteCritico)) {
            throw new IllegalArgumentException("o limite de alerta deve ser menor que o crítico");
        }
        this.janela = janela;
        this.limiteAlerta = limiteAlerta;
        this.limiteCritico = limiteCritico;
    }

    @Override
    public Nivel avaliar(List<Double> leituras) {
        int n = leituras.size();
        double soma = 0;
        int inicio = Math.max(0, n - janela);
        for (int i = inicio; i < n; i++) {
            soma += leituras.get(i);
        }
        double media = soma / (n - inicio);
        if (media >= limiteCritico) return Nivel.CRITICO;
        if (media >= limiteAlerta) return Nivel.ALERTA;
        return Nivel.NORMAL;
    }
}
