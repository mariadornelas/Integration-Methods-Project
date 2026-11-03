package com.faculdade.sensor;

public class SensorTemperatura extends Sensor {

    public SensorTemperatura(String id, FonteLeitura fonte, EstrategiaAvaliacao estrategia) {
        super(id, fonte, estrategia);
    }

    @Override
    protected String tipo() {
        return "TEMPERATURA";
    }

    @Override
    protected String unidade() {
        return "°C";
    }

    @Override
    protected double minimoFisico() {
        return -50.0;
    }

    @Override
    protected double maximoFisico() {
        return 1000.0;
    }
}
