package com.faculdade.sensor;

import java.util.Objects;

public record Medicao(String sensorId, String tipo, double valor, String unidade, Nivel nivel) {

    public Medicao {
        Objects.requireNonNull(sensorId, "sensorId não pode ser nulo");
        Objects.requireNonNull(tipo, "tipo não pode ser nulo");
        Objects.requireNonNull(unidade, "unidade não pode ser nula");
        Objects.requireNonNull(nivel, "nível não pode ser nulo");
    }
}
