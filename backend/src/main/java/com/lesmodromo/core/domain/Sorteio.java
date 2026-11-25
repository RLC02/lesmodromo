package com.lesmodromo.core.domain;

/**
 * Fonte de aleatoriedade da corrida. É uma interface para os testes poderem usar um valor fixo.
 */
@FunctionalInterface
public interface Sorteio {

    /** Número entre 0 (inclusive) e 1 (exclusive). */
    double proximo();
}
