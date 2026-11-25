package com.lesmodromo.core.domain;

/**
 * Ciclo de vida da corrida:
 * NA_LARGADA (aposta registrada e evento publicado) -> CORRENDO (juiz consumiu a mensagem) -> FINALIZADA.
 */
public enum StatusCorrida {
    NA_LARGADA,
    CORRENDO,
    FINALIZADA
}
