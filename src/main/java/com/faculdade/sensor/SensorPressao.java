package com.faculdade.sensor;

public class SensorPressao extends Sensor {

    public SensorPressao(String id, FonteLeitura fonte, EstrategiaAvaliacao estrategia) {
        super(id, fonte, estrategia);
    }

    @Override
    protected double converter(double bruto) {
        return bruto / 100.0;   // 100 kPa = 1 bar
    }

    @Override
    protected String tipo() {
        return "PRESSAO";
    }

    @Override
    protected String unidade() {
        return "bar";
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
