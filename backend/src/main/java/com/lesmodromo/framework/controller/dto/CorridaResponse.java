package com.lesmodromo.framework.controller.dto;

import com.lesmodromo.core.domain.Clima;
import com.lesmodromo.core.domain.Corrida;

import java.time.Instant;
import java.util.List;

/**
 * O que o front recebe sobre uma corrida. Os campos rankingAtualizado e narrada mostram,
 * na tela, quando cada consumidor do RabbitMQ terminou a sua parte.
 */
public record CorridaResponse(
        String id,
        String jogador,
        String lesmaEscolhida,
        String cidade,
        String status,
        boolean modoCaos,
        ClimaResponse clima,
        boolean climaFallback,
        int tentativasClima,
        List<ColocacaoResponse> resultado,
        String vencedora,
        boolean venceu,
        int pontosGanhos,
        boolean rankingAtualizado,
        boolean narrada,
        Instant criadaEm) {

    public record ClimaResponse(double temperatura, double umidade, double vento, double chuva) {
    }

    public record ColocacaoResponse(int posicao, String lesma, String nome, double pontuacao, double bonus) {
    }

    public static CorridaResponse de(Corrida corrida) {
        Clima clima = corrida.getClima();
        boolean finalizada = corrida.finalizada();

        return new CorridaResponse(
                corrida.getId(),
                corrida.getJogador(),
                corrida.getLesmaEscolhida().name(),
                corrida.getCidade().getNome(),
                corrida.getStatus().name(),
                corrida.isModoCaos(),
                clima == null ? null : new ClimaResponse(clima.temperatura(), clima.umidade(), clima.vento(), clima.chuva()),
                corrida.isClimaFallback(),
                corrida.getTentativasClima(),
                corrida.getResultado().stream()
                        .map(c -> new ColocacaoResponse(c.posicao(), c.lesma().name(), c.lesma().getNome(), c.pontuacao(), c.bonus()))
                        .toList(),
                finalizada ? corrida.vencedora().name() : null,
                corrida.venceu(),
                finalizada ? corrida.pontosGanhos() : 0,
                corrida.isRankingAtualizado(),
                corrida.isNarrada(),
                corrida.getCriadaEm());
    }
}
