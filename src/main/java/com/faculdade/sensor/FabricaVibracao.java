package com.faculdade.sensor;

public class FabricaVibracao extends FabricaSensor {

    @Override
    protected Sensor criarSensor(String id, FonteLeitura fonte) {
        return new SensorVibracao(id, fonte, new MediaMovel(3, 5, 8));
    }
}
