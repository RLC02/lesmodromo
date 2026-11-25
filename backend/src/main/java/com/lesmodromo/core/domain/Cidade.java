package com.lesmodromo.core.domain;

import com.lesmodromo.core.exception.RegraDeNegocioException;

/**
 * Cidades onde a corrida pode acontecer. O clima real de cada uma decide a prova.
 */
public enum Cidade {

    SAO_PAULO("São Paulo", -23.55, -46.63),
    MANAUS("Manaus", -3.12, -60.02),
    PORTO_ALEGRE("Porto Alegre", -30.03, -51.23),
    FORTALEZA("Fortaleza", -3.73, -38.52);

    private final String nome;
    private final double latitude;
    private final double longitude;

    Cidade(String nome, double latitude, double longitude) {
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Cidade porCodigo(String codigo) {
        for (Cidade cidade : values()) {
            if (cidade.name().equalsIgnoreCase(codigo)) {
                return cidade;
            }
        }
        throw new RegraDeNegocioException("Cidade inválida: " + codigo);
    }

    public String getNome() {
        return nome;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
