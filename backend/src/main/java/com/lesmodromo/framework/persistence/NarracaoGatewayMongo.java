package com.lesmodromo.framework.persistence;

import com.lesmodromo.core.domain.Narracao;
import com.lesmodromo.core.gateway.NarracaoGateway;
import com.lesmodromo.framework.persistence.document.NarracaoDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class NarracaoGatewayMongo implements NarracaoGateway {

    private final MongoTemplate mongoTemplate;

    public NarracaoGatewayMongo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void salvar(Narracao narracao) {
        mongoTemplate.insert(new NarracaoDocument(UUID.randomUUID().toString(), narracao.idCorrida(), narracao.texto(), narracao.criadaEm()));
    }

    @Override
    public List<Narracao> ultimas(int limite) {
        Query recentes = new Query().with(Sort.by(Sort.Direction.DESC, "criadaEm")).limit(limite);
        return mongoTemplate.find(recentes, NarracaoDocument.class).stream()
                .map(n -> new Narracao(n.idCorrida(), n.texto(), n.criadaEm()))
                .toList();
    }
}
