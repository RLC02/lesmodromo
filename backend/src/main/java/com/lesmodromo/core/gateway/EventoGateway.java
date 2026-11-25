package com.lesmodromo.core.gateway;

import com.lesmodromo.core.domain.Corrida;

/**
 * Porta de saída para a mensageria. Quem implementa é a camada framework (RabbitMQ).
 */
public interface EventoGateway {

    /** Avisa que uma aposta foi feita. Quem escuta: o juiz. */
    void publicarCorridaLargada(Corrida corrida);

    /** Avisa que a corrida acabou. Quem escuta: o ranking e o narrador. */
    void publicarCorridaFinalizada(Corrida corrida);
}
