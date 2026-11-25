package com.lesmodromo.framework.resilience;

/**
 * Todas as tentativas falharam.
 */
public class RetryEsgotadoException extends RuntimeException {

    private final int tentativas;

    public RetryEsgotadoException(String nome, int tentativas, Throwable causa) {
        super(nome + " falhou " + tentativas + " vezes" + (causa != null ? ": " + causa.getMessage() : ""), causa);
        this.tentativas = tentativas;
    }

    public int getTentativas() {
        return tentativas;
    }
}
