package com.lesmodromo.framework.persistence;

import com.lesmodromo.core.domain.Corrida;
import com.lesmodromo.core.gateway.CorridaGateway;
import com.lesmodromo.framework.persistence.document.CorridaDocument;
import com.lesmodromo.framework.resilience.RetryExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * RETRY 2: gravações no MongoDB Atlas.
 * O banco está em outra rede; uma oscilação ou troca de primário no cluster derruba uma gravação.
 * Salvar com retry evita perder a aposta por causa de uma falha de segundos.
 */
@Component
public class CorridaGatewayMongo implements CorridaGateway {

    private static final int TENTATIVAS = 3;
    private static final Duration ESPERA = Duration.ofMillis(300);

    private final CorridaRepository repository;
    private final MongoTemplate mongoTemplate;
    private final RetryExecutor retry;

    public CorridaGatewayMongo(CorridaRepository repository, MongoTemplate mongoTemplate, RetryExecutor retry) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
        this.retry = retry;
    }

    @Override
    public Corrida salvar(Corrida corrida) {
        CorridaDocument salvo = retry.executar("MongoDB Atlas (salvar corrida)", TENTATIVAS, ESPERA,
                tentativa -> repository.save(CorridaMapper.paraDocumento(corrida)));
        return CorridaMapper.paraDominio(salvo);
    }

    @Override
    public Optional<Corrida> buscarPorId(String id) {
        return repository.findById(id).map(CorridaMapper::paraDominio);
    }

    @Override
    public Optional<Corrida> buscarPorIdCliente(String idCliente) {
        return repository.findByIdCliente(idCliente).map(CorridaMapper::paraDominio);
    }

    @Override
    public boolean marcarRankingAtualizado(String id) {
        // Só marca se ainda estiver false. Se outra entrega da mesma mensagem já marcou, não altera nada.
        Query ainda = Query.query(Criteria.where("_id").is(id).and("rankingAtualizado").is(false));
        long alterados = mongoTemplate.updateFirst(ainda, Update.update("rankingAtualizado", true), CorridaDocument.class)
                .getModifiedCount();
        return alterados == 1;
    }

    @Override
    public void marcarNarrada(String id) {
        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(id)), Update.update("narrada", true),
                CorridaDocument.class);
    }
}
