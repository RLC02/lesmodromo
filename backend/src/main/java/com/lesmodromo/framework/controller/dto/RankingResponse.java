package com.lesmodromo.framework.controller.dto;

import com.lesmodromo.core.domain.Ranking;

import java.util.List;

public record RankingResponse(List<JogadorResponse> jogadores, List<LesmaVitoriasResponse> lesmas) {

    public record JogadorResponse(String nome, int pontos, int corridas, int vitorias) {
    }

    public record LesmaVitoriasResponse(String lesma, String nome, int vitorias) {
    }

    public static RankingResponse de(Ranking ranking) {
        return new RankingResponse(
                ranking.jogadores().stream()
                        .map(j -> new JogadorResponse(j.nome(), j.pontos(), j.corridas(), j.vitorias()))
                        .toList(),
                ranking.lesmas().stream()
                        .map(v -> new LesmaVitoriasResponse(v.lesma().name(), v.lesma().getNome(), v.vitorias()))
                        .toList());
    }
}
