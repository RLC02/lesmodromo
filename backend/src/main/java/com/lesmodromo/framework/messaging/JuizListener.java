package com.lesmodromo.framework.messaging;

import com.lesmodromo.core.usecase.ProcessarCorrida;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor 1: o juiz. Escuta "corrida.largada", faz a corrida acontecer e publica "corrida.finalizada".
 *
 * RETRY 3b: se o processamento lançar erro, o Spring AMQP tenta de novo (configurado no application.yml:
 * spring.rabbitmq.listener.simple.retry). Depois da 3ª falha a mensagem vai para a fila lesmodromo.falhas.
 */
@Component
public class JuizListener {

    private static final Logger log = LoggerFactory.getLogger(JuizListener.class);

    private final ProcessarCorrida processarCorrida;

    public JuizListener(ProcessarCorrida processarCorrida) {
        this.processarCorrida = processarCorrida;
    }

    @RabbitListener(queues = RabbitConfig.FILA_JUIZ)
    public void receber(CorridaEvento evento) {
        log.info("[mensageria] juiz consumiu corrida.largada (corrida {})", evento.idCorrida());
        processarCorrida.executar(evento.idCorrida());
    }
}
