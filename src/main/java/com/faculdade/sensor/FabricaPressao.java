package com.faculdade.sensor;

public class FabricaPressao extends FabricaSensor {

    @Override
    protected Sensor criarSensor(String id, FonteLeitura fonte) {
        return new SensorPressao(id, fonte, new TaxaVariacao(1.5, 3.0));
    }
}
