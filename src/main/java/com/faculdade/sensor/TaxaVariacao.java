package com.faculdade.sensor;

import java.util.List;

public class TaxaVariacao implements EstrategiaAvaliacao {

    private final double variacaoAlerta;
    private final double variacaoCritica;

    public TaxaVariacao(double variacaoAlerta, double variacaoCritica) {
        if (variacaoAlerta <= 0 || !(variacaoAlerta < variacaoCritica)) {
            throw new IllegalArgumentException("exige 0 < variação de alerta < variação crítica");
        }
        this.variacaoAlerta = variacaoAlerta;
        this.variacaoCritica = variacaoCritica;
    }

    @Override
    public Nivel avaliar(List<Double> leituras) {
        int n = leituras.size();
        if (n < 2) return Nivel.NORMAL;
        double variacao = Math.abs(leituras.get(n - 1) - leituras.get(n - 2));
        if (variacao >= variacaoCritica) return Nivel.CRITICO;
        if (variacao >= variacaoAlerta) return Nivel.ALERTA;
        return Nivel.NORMAL;
    }
}
