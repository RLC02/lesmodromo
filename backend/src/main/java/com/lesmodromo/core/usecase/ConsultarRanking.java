package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Ranking;
import com.lesmodromo.core.gateway.RankingGateway;

public class ConsultarRanking {

    private static final int TOP = 10;

    private final RankingGateway ranking;

    public ConsultarRanking(RankingGateway ranking) {
        this.ranking = ranking;
    }

    public Ranking executar() {
        return new Ranking(ranking.melhores(TOP), ranking.vitoriasPorLesma());
    }
}
