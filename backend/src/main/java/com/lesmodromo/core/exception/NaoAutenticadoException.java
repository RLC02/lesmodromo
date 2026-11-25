package com.lesmodromo.core.exception;

/**
 * Sem login, login errado ou sessão vencida. Vira HTTP 401.
 */
public class NaoAutenticadoException extends RuntimeException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
