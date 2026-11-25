package com.lesmodromo.framework.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parte da resposta da Open-Meteo que interessa para a corrida.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResposta(Atual current) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Atual(
            @JsonProperty("temperature_2m") double temperatura,
            @JsonProperty("relative_humidity_2m") double umidade,
            @JsonProperty("wind_speed_10m") double vento,
            @JsonProperty("precipitation") double chuva) {
    }
}
