package com.lesmodromo.core.domain;

import java.util.List;

/**
 * Placar geral: melhores apostadores e lesmas que mais venceram.
 */
public record Ranking(List<Jogador> jogadores, List<VitoriasDaLesma> lesmas) {
}
