package com.faculdade.sensor;

public abstract class FabricaSensor {

    public final Sensor criar(String id, FonteLeitura fonte) {
        Sensor sensor = criarSensor(id, fonte);
        CentralMonitoramento.getInstancia().registrar(sensor);
        return sensor;
    }

    protected abstract Sensor criarSensor(String id, FonteLeitura fonte);
}
