package com.lesmodromo.framework.messaging;

import com.lesmodromo.core.domain.Corrida;
import com.lesmodromo.core.gateway.EventoGateway;
import com.lesmodromo.framework.resilience.RetryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Produtor: publica os eventos no exchange do RabbitMQ.
 *
 * RETRY 3a: publicar com retry. Se a conexão com a CloudAMQP oscilar, tenta de novo.
 * Se não der nem assim, a exceção sobe e o caso de uso faz o fallback (processa direto).
 */
@Component
public class EventoGatewayRabbit implements EventoGateway {

    private static final Logger log = LoggerFactory.getLogger(EventoGatewayRabbit.class);
    private static final int TENTATIVAS = 3;
    private static final Duration ESPERA = Duration.ofMillis(300);

    private final RabbitTemplate rabbitTemplate;
    private final RetryExecutor retry;

    public EventoGatewayRabbit(RabbitTemplate rabbitTemplate, RetryExecutor retry) {
        this.rabbitTemplate = rabbitTemplate;
        this.retry = retry;
    }

    @Override
    public void publicarCorridaLargada(Corrida corrida) {
        publicar(RabbitConfig.CORRIDA_LARGADA, corrida);
    }

    @Override
    public void publicarCorridaFinalizada(Corrida corrida) {
        publicar(RabbitConfig.CORRIDA_FINALIZADA, corrida);
    }

    private void publicar(String tipoDoEvento, Corrida corrida) {
        retry.executar("RabbitMQ (publicar " + tipoDoEvento + ")", TENTATIVAS, ESPERA,
                () -> rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, tipoDoEvento, new CorridaEvento(corrida.getId())));
        log.info("[mensageria] publicado {} (corrida {})", tipoDoEvento, corrida.getId());
    }
}
