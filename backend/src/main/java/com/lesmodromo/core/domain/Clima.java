package com.lesmodromo.core.domain;

/**
 * Clima no momento da corrida.
 *
 * @param temperatura em °C
 * @param umidade     em %
 * @param vento       em km/h
 * @param chuva       em mm
 */
public record Clima(double temperatura, double umidade, double vento, double chuva) {

    /** Usado quando a API de clima não responde nem depois do retry (fallback). */
    public static final Clima PADRAO = new Clima(22, 60, 10, 0);

    public boolean chovendo() {
        return chuva > 0;
    }
}
