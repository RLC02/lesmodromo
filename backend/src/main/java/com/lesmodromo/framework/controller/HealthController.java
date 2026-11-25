package com.lesmodromo.framework.controller;

import org.bson.Document;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Usado pelo Render (health check) e pela pipeline: ela espera aparecer aqui a versão do commit novo.
 */
@RestController
public class HealthController {

    private final MongoTemplate mongoTemplate;
    private final ConnectionFactory rabbitConnectionFactory;
    private final String versao;

    public HealthController(MongoTemplate mongoTemplate, ConnectionFactory rabbitConnectionFactory,
                            @Value("${lesmodromo.versao}") String versao) {
        this.mongoTemplate = mongoTemplate;
        this.rabbitConnectionFactory = rabbitConnectionFactory;
        this.versao = versao;
    }

    /** Página inicial da API. O Render e o navegador costumam bater aqui. */
    @GetMapping("/")
    public Map<String, String> inicio() {
        return Map.of("mensagem", "Lesmódromo API no ar. Veja /health e /api/info");
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new LinkedHashMap<>();
        status.put("status", "ok");
        status.put("versao", versao);
        status.put("mongo", mongoConectado() ? "conectado" : "desconectado");
        status.put("rabbit", rabbitConectado() ? "conectado" : "desconectado");
        return status;
    }

    private boolean mongoConectado() {
        try {
            mongoTemplate.executeCommand(new Document("ping", 1));
            return true;
        } catch (RuntimeException erro) {
            return false;
        }
    }

    private boolean rabbitConectado() {
        try {
            return rabbitConnectionFactory.createConnection().isOpen();
        } catch (RuntimeException erro) {
            return false;
        }
    }
}
