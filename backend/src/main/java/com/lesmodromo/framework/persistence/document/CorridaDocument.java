package com.lesmodromo.framework.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Como a corrida fica gravada no MongoDB (coleção "corridas").
 * É separado da entidade Corrida do core para o domínio não depender do banco.
 */
@Document("corridas")
public record CorridaDocument(
        @Id String id,
        String idCliente,
        String jogador,
        String lesmaEscolhida,
        String cidade,
        boolean modoCaos,
        Instant criadaEm,
        String status,
        ClimaDocument clima,
        boolean climaFallback,
        int tentativasClima,
        List<ColocacaoDocument> resultado,
        String vencedora,
        boolean rankingAtualizado,
        boolean narrada) {

    public record ClimaDocument(double temperatura, double umidade, double vento, double chuva) {
    }

    public record ColocacaoDocument(int posicao, String lesma, double pontuacao, double bonus) {
    }
}
