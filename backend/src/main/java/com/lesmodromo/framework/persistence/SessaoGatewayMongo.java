package com.lesmodromo.framework.persistence;

import com.lesmodromo.core.domain.Sessao;
import com.lesmodromo.core.gateway.SessaoGateway;
import com.lesmodromo.framework.persistence.document.SessaoDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SessaoGatewayMongo implements SessaoGateway {

    private final MongoTemplate mongoTemplate;

    public SessaoGatewayMongo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void salvar(Sessao sessao) {
        mongoTemplate.insert(new SessaoDocument(sessao.tokenHash(), sessao.usuarioId(), sessao.expiraEm()));
    }

    @Override
    public Optional<Sessao> buscar(String tokenHash) {
        return Optional.ofNullable(mongoTemplate.findById(tokenHash, SessaoDocument.class))
                .map(doc -> new Sessao(doc.tokenHash(), doc.usuarioId(), doc.expiraEm()));
    }

    @Override
    public void remover(String tokenHash) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(tokenHash)), SessaoDocument.class);
    }
}
