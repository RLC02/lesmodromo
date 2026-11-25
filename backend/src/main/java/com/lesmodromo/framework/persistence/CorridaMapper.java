package com.lesmodromo.framework.persistence;

import com.lesmodromo.core.domain.Cidade;
import com.lesmodromo.core.domain.Clima;
import com.lesmodromo.core.domain.Colocacao;
import com.lesmodromo.core.domain.Corrida;
import com.lesmodromo.core.domain.Lesma;
import com.lesmodromo.core.domain.StatusCorrida;
import com.lesmodromo.framework.persistence.document.CorridaDocument;
import com.lesmodromo.framework.persistence.document.CorridaDocument.ClimaDocument;
import com.lesmodromo.framework.persistence.document.CorridaDocument.ColocacaoDocument;

import java.util.List;

/**
 * Converte entre a entidade do core e o documento do MongoDB.
 */
final class CorridaMapper {

    private CorridaMapper() {
    }

    static CorridaDocument paraDocumento(Corrida corrida) {
        Clima clima = corrida.getClima();
        List<ColocacaoDocument> resultado = corrida.getResultado().stream()
                .map(c -> new ColocacaoDocument(c.posicao(), c.lesma().name(), c.pontuacao(), c.bonus()))
                .toList();

        return new CorridaDocument(
                corrida.getId(),
                corrida.getIdCliente(),
                corrida.getJogador(),
                corrida.getLesmaEscolhida().name(),
                corrida.getCidade().name(),
                corrida.isModoCaos(),
                corrida.getCriadaEm(),
                corrida.getStatus().name(),
                clima == null ? null : new ClimaDocument(clima.temperatura(), clima.umidade(), clima.vento(), clima.chuva()),
                corrida.isClimaFallback(),
                corrida.getTentativasClima(),
                resultado,
                corrida.vencedora() == null ? null : corrida.vencedora().name(),
                corrida.isRankingAtualizado(),
                corrida.isNarrada());
    }

    static Corrida paraDominio(CorridaDocument doc) {
        ClimaDocument clima = doc.clima();
        List<Colocacao> resultado = doc.resultado() == null ? List.of() : doc.resultado().stream()
                .map(c -> new Colocacao(c.posicao(), Lesma.valueOf(c.lesma()), c.pontuacao(), c.bonus()))
                .toList();

        return new Corrida(
                doc.id(),
                doc.idCliente(),
                doc.jogador(),
                Lesma.valueOf(doc.lesmaEscolhida()),
                Cidade.valueOf(doc.cidade()),
                doc.modoCaos(),
                doc.criadaEm(),
                StatusCorrida.valueOf(doc.status()),
                clima == null ? null : new Clima(clima.temperatura(), clima.umidade(), clima.vento(), clima.chuva()),
                doc.climaFallback(),
                doc.tentativasClima(),
                resultado,
                doc.rankingAtualizado(),
                doc.narrada());
    }
}
