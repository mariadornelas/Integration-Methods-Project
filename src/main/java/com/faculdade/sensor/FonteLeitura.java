package com.faculdade.sensor;

@FunctionalInterface
public interface FonteLeitura {

    double ler();

    static FonteLeitura sequencia(double... valores) {
        final double[] copia = valores.clone();
        final int[] indice = {0};
        return () -> {
            if (indice[0] >= copia.length) {
                throw new IllegalStateException("a fonte simulada não tem mais leituras");
            }
            return copia[indice[0]++];
        };
    }
}
