package com.lesmodromo.core.domain;

/**
 * Posição de uma lesma no resultado final.
 */
public record Colocacao(int posicao, Lesma lesma, double pontuacao, double bonus) {
}
