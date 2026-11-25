package com.lesmodromo.core.exception;

/**
 * A API de clima não respondeu nem depois de todas as tentativas do retry.
 * O caso de uso trata isso com fallback (clima padrão).
 */
public class ClimaIndisponivelException extends RuntimeException {

    private final int tentativas;

    public ClimaIndisponivelException(int tentativas, Throwable causa) {
        super("API de clima indisponível depois de " + tentativas + " tentativas", causa);
        this.tentativas = tentativas;
    }

    public int getTentativas() {
        return tentativas;
    }
}
