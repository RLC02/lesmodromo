package com.lesmodromo.core.exception;

/**
 * Vira HTTP 404.
 */
public class CorridaNaoEncontradaException extends RuntimeException {

    public CorridaNaoEncontradaException(String idCorrida) {
        super("Corrida não encontrada: " + idCorrida);
    }
}
