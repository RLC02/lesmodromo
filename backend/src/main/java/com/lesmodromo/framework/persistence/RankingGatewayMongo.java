package com.lesmodromo.framework.persistence;

import com.lesmodromo.core.domain.Jogador;
import com.lesmodromo.core.domain.Lesma;
import com.lesmodromo.core.domain.StatusCorrida;
import com.lesmodromo.core.domain.VitoriasDaLesma;
import com.lesmodromo.core.gateway.RankingGateway;
import com.lesmodromo.framework.persistence.document.CorridaDocument;
import com.lesmodromo.framework.persistence.document.JogadorDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RankingGatewayMongo implements RankingGateway {

    private final MongoTemplate mongoTemplate;

    public RankingGatewayMongo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void registrar(String jogador, int pontos, boolean venceu) {
        // $inc no próprio banco: duas corridas do mesmo jogador ao mesmo tempo não se atropelam
        Update soma = new Update()
                .inc("pontos", pontos)
                .inc("corridas", 1)
                .inc("vitorias", venceu ? 1 : 0);
        mongoTemplate.upsert(Query.query(Criteria.where("nome").is(jogador)), soma, JogadorDocument.class);
    }

    @Override
    public List<Jogador> melhores(int limite) {
        Query top = new Query().with(Sort.by(Sort.Direction.DESC, "pontos")).limit(limite);
        return mongoTemplate.find(top, JogadorDocument.class).stream()
                .map(j -> new Jogador(j.nome(), j.pontos(), j.corridas(), j.vitorias()))
                .toList();
    }

    @Override
    public List<VitoriasDaLesma> vitoriasPorLesma() {
        Aggregation contagem = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is(StatusCorrida.FINALIZADA.name())),
                Aggregation.group("vencedora").count().as("vitorias"),
                Aggregation.sort(Sort.Direction.DESC, "vitorias"));

        return mongoTemplate.aggregate(contagem, CorridaDocument.class, VitoriasPorLesma.class)
                .getMappedResults().stream()
                .filter(v -> v.id() != null)
                .map(v -> new VitoriasDaLesma(Lesma.valueOf(v.id()), v.vitorias()))
                .toList();
    }

    /** Formato da resposta da agregação: { _id: "GOSMINHA", vitorias: 3 } */
    public record VitoriasPorLesma(String id, int vitorias) {
    }
}
