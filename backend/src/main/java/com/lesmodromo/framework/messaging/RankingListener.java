package com.lesmodromo.framework.messaging;

import com.lesmodromo.core.usecase.AtualizarRanking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor 2: escuta "corrida.finalizada" e soma os pontos do apostador.
 */
@Component
public class RankingListener {

    private static final Logger log = LoggerFactory.getLogger(RankingListener.class);

    private final AtualizarRanking atualizarRanking;

    public RankingListener(AtualizarRanking atualizarRanking) {
        this.atualizarRanking = atualizarRanking;
    }

    @RabbitListener(queues = RabbitConfig.FILA_RANKING)
    public void receber(CorridaEvento evento) {
        log.info("[mensageria] ranking consumiu corrida.finalizada (corrida {})", evento.idCorrida());
        atualizarRanking.executar(evento.idCorrida());
    }
}
