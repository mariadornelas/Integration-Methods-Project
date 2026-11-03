package com.faculdade.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public abstract class Sensor {

    private final String id;
    private final FonteLeitura fonte;
    private final List<Double> historico = new ArrayList<>();
    private EstrategiaAvaliacao estrategia;

    protected Sensor(String id, FonteLeitura fonte, EstrategiaAvaliacao estrategia) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id não pode ser vazio");
        }
        this.id = id;
        this.fonte = Objects.requireNonNull(fonte, "fonte não pode ser nula");
        this.estrategia = Objects.requireNonNull(estrategia, "estratégia não pode ser nula");
    }

    public final Medicao medir() {
        double bruto = fonte.ler();
        double valor = converter(bruto);
        validar(valor);
        historico.add(valor);
        Nivel nivel = estrategia.avaliar(Collections.unmodifiableList(historico));   // 5. Strategy
        return new Medicao(id, tipo(), valor, unidade(), nivel);
    }

    protected double converter(double bruto) {
        return bruto;
    }

    protected void validar(double valor) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)
                || valor < minimoFisico() || valor > maximoFisico()) {
            throw new IllegalStateException(String.format(Locale.ROOT,
                    "leitura fisicamente impossível em %s: %s %s (faixa %.1f a %.1f)",
                    id, valor, unidade(), minimoFisico(), maximoFisico()));
        }
    }

    protected abstract String tipo();

    protected abstract String unidade();

    protected abstract double minimoFisico();

    protected abstract double maximoFisico();

    public void trocarEstrategia(EstrategiaAvaliacao nova) {
        this.estrategia = Objects.requireNonNull(nova, "estratégia não pode ser nula");
    }

    public EstrategiaAvaliacao getEstrategia() {
        return estrategia;
    }

    public String getId() {
        return id;
    }

    public List<Double> getHistorico() {
        return Collections.unmodifiableList(historico);
    }
}
