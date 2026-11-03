package com.faculdade.sensor;

import java.util.List;

@FunctionalInterface
public interface EstrategiaAvaliacao {

    Nivel avaliar(List<Double> leituras);
}
