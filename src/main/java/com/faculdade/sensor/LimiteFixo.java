package com.faculdade.sensor;

import java.util.List;

public class LimiteFixo implements EstrategiaAvaliacao {

    private final double limiteAlerta;
    private final double limiteCritico;

    public LimiteFixo(double limiteAlerta, double limiteCritico) {
        if (!(limiteAlerta < limiteCritico)) {
            throw new IllegalArgumentException("o limite de alerta deve ser menor que o crítico");
        }
        this.limiteAlerta = limiteAlerta;
        this.limiteCritico = limiteCritico;
    }

    @Override
    public Nivel avaliar(List<Double> leituras) {
        double atual = leituras.get(leituras.size() - 1);
        if (atual >= limiteCritico) return Nivel.CRITICO;
        if (atual >= limiteAlerta) return Nivel.ALERTA;
        return Nivel.NORMAL;
    }
}
