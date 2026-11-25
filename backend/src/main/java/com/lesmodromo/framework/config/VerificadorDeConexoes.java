package com.lesmodromo.framework.config;

import com.lesmodromo.framework.persistence.document.CorridaDocument;
import com.lesmodromo.framework.persistence.document.JogadorDocument;
import com.lesmodromo.framework.resilience.RetryExecutor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * RETRY na subida do servidor: assim que a aplicação fica pronta, confirma que o MongoDB Atlas
 * e o RabbitMQ (CloudAMQP) estão respondendo, tentando de novo por quase 2 minutos se não estiverem.
 *
 * Com SIMULAR_FALHAS_NA_SUBIDA=true, a 1ª tentativa de cada um falha de propósito (para a apresentação).
 */
@Component
public class VerificadorDeConexoes {

    private static final Logger log = LoggerFactory.getLogger(VerificadorDeConexoes.class);
    private static final int TENTATIVAS = 10;
    private static final Duration ESPERA = Duration.ofSeconds(1);

    private final MongoTemplate mongoTemplate;
    private final ConnectionFactory rabbitConnectionFactory;
    private final RetryExecutor retry;
    private final int falhasSimuladas;

    public VerificadorDeConexoes(MongoTemplate mongoTemplate, ConnectionFactory rabbitConnectionFactory, RetryExecutor retry,
                                 @Value("${lesmodromo.simular-falhas-na-subida}") boolean simularFalhas) {
        this.mongoTemplate = mongoTemplate;
        this.rabbitConnectionFactory = rabbitConnectionFactory;
        this.retry = retry;
        this.falhasSimuladas = simularFalhas ? 1 : 0;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verificar() {
        try {
            retry.executar("MongoDB Atlas", TENTATIVAS, ESPERA, falhasSimuladas,
                    tentativa -> mongoTemplate.executeCommand(new Document("ping", 1)));
            criarIndices();
            log.info("[mongo] conectado ao Atlas");
        } catch (RuntimeException erro) {
            log.error("[mongo] não foi possível conectar: {}", erro.getMessage());
        }

        try {
            retry.executar("RabbitMQ (CloudAMQP)", TENTATIVAS, ESPERA, falhasSimuladas, tentativa -> {
                rabbitConnectionFactory.createConnection().close();
                return true;
            });
            log.info("[rabbit] conectado à CloudAMQP");
        } catch (RuntimeException erro) {
            log.error("[rabbit] não foi possível conectar: {}", erro.getMessage());
        }
    }

    private void criarIndices() {
        // idCliente único: garante no banco que o retry do front não cria corrida duplicada
        mongoTemplate.indexOps(CorridaDocument.class)
                .ensureIndex(new Index().on("idCliente", Sort.Direction.ASC).unique().sparse());
        mongoTemplate.indexOps(JogadorDocument.class)
                .ensureIndex(new Index().on("nome", Sort.Direction.ASC).unique());
    }
}
