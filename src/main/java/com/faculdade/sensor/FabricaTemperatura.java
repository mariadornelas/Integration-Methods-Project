package com.faculdade.sensor;

public class FabricaTemperatura extends FabricaSensor {

    @Override
    protected Sensor criarSensor(String id, FonteLeitura fonte) {
        return new SensorTemperatura(id, fonte, new LimiteFixo(80, 90));
    }
}
