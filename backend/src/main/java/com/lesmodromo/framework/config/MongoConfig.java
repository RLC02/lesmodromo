package com.lesmodromo.framework.config;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

import java.util.concurrent.TimeUnit;

@Configuration
public class MongoConfig {

    /** Liga as transações do MongoDB (usadas pela porta Transacao do core). */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }

    /**
     * Sem isso, cada tentativa de falar com um Atlas fora do ar espera 30 s antes de desistir.
     * Com 5 s, o retry consegue fazer várias tentativas em pouco tempo.
     */
    @Bean
    public MongoClientSettingsBuilderCustomizer timeoutsCurtos() {
        return settings -> settings
                .applyToClusterSettings(cluster -> cluster.serverSelectionTimeout(5, TimeUnit.SECONDS))
                .applyToSocketSettings(socket -> socket.connectTimeout(5, TimeUnit.SECONDS));
    }
}
