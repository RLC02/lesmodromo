package com.lesmodromo.core.exception;

/**
 * Pedido que quebra alguma regra do jogo (nome vazio, lesma que não existe...). Vira HTTP 400.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
