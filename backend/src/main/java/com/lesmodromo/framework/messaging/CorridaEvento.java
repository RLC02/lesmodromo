package com.lesmodromo.framework.messaging;

/**
 * Conteúdo das mensagens. Vai só o id: cada consumidor busca no banco o estado mais atual da corrida.
 */
public record CorridaEvento(String idCorrida) {
}
