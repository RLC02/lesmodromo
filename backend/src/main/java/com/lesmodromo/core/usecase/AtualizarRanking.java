package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Corrida;
import com.lesmodromo.core.exception.CorridaNaoEncontradaException;
import com.lesmodromo.core.exception.RegraDeNegocioException;
import com.lesmodromo.core.gateway.CorridaGateway;
import com.lesmodromo.core.gateway.RankingGateway;
import com.lesmodromo.core.gateway.Transacao;

/**
 * Soma os pontos do jogador. É chamado pelo consumidor da fila do ranking.
 * Marcar a corrida como contabilizada e somar os pontos acontecem na MESMA transação:
 * se uma das gravações falhar, nenhuma vale, e a mensagem pode ser reprocessada sem contar pontos duas vezes.
 */
public class AtualizarRanking {

    private final CorridaGateway corridas;
    private final RankingGateway ranking;
    private final Transacao transacao;

    public AtualizarRanking(CorridaGateway corridas, RankingGateway ranking, Transacao transacao) {
        this.corridas = corridas;
        this.ranking = ranking;
        this.transacao = transacao;
    }

    public void executar(String idCorrida) {
        Corrida corrida = corridas.buscarPorId(idCorrida).orElseThrow(() -> new CorridaNaoEncontradaException(idCorrida));
        if (!corrida.finalizada()) {
            throw new RegraDeNegocioException("A corrida " + idCorrida + " ainda não terminou");
        }

        transacao.executar(() -> {
            boolean primeiraVez = corridas.marcarRankingAtualizado(idCorrida);
            if (primeiraVez) {
                ranking.registrar(corrida.getJogador(), corrida.pontosGanhos(), corrida.venceu());
            }
        });
    }
}
