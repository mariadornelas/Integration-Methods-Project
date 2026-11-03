package com.faculdade.sensor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class CentralMonitoramento {

    private static final class Holder {
        private static final CentralMonitoramento INSTANCIA = new CentralMonitoramento();
    }

    private final Map<String, Sensor> sensores = new LinkedHashMap<>();

    private CentralMonitoramento() {
    }

    public static CentralMonitoramento getInstancia() {
        return Holder.INSTANCIA;
    }

    public synchronized void registrar(Sensor sensor) {
        Objects.requireNonNull(sensor, "sensor não pode ser nulo");
        if (sensores.containsKey(sensor.getId())) {
            throw new IllegalStateException("já existe um sensor com o id " + sensor.getId());
        }
        sensores.put(sensor.getId(), sensor);
    }

    public synchronized Optional<Sensor> buscar(String id) {
        return Optional.ofNullable(sensores.get(id));
    }

    public synchronized List<Medicao> medirTodos() {
        List<Medicao> medicoes = new ArrayList<>();
        for (Sensor sensor : sensores.values()) {
            medicoes.add(sensor.medir());
        }
        return medicoes;
    }

    public synchronized int quantidade() {
        return sensores.size();
    }

    public synchronized void limpar() {
        sensores.clear();
    }
}
