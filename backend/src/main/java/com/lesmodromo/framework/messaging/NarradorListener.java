package com.lesmodromo.framework.messaging;

import com.lesmodromo.core.usecase.NarrarCorrida;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor 3: escuta o MESMO "corrida.finalizada" que o ranking (pub/sub) e cria a frase da Rádio Lesma.
 */
@Component
public class NarradorListener {

    private static final Logger log = LoggerFactory.getLogger(NarradorListener.class);

    private final NarrarCorrida narrarCorrida;

    public NarradorListener(NarrarCorrida narrarCorrida) {
        this.narrarCorrida = narrarCorrida;
    }

    @RabbitListener(queues = RabbitConfig.FILA_NARRADOR)
    public void receber(CorridaEvento evento) {
        log.info("[mensageria] narrador consumiu corrida.finalizada (corrida {})", evento.idCorrida());
        narrarCorrida.executar(evento.idCorrida());
    }
}
