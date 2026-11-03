package com.faculdade.sensor;

public class SensorVibracao extends Sensor {

    public SensorVibracao(String id, FonteLeitura fonte, EstrategiaAvaliacao estrategia) {
        super(id, fonte, estrategia);
    }

    @Override
    protected String tipo() {
        return "VIBRACAO";
    }

    @Override
    protected String unidade() {
        return "mm/s";
    }

    @Override
    protected double minimoFisico() {
        return 0.0;
    }

    @Override
    protected double maximoFisico() {
        return 100.0;
    }
}
