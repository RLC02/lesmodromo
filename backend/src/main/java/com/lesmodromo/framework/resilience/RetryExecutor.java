package com.lesmodromo.framework.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.IntFunction;

/**
 * O retry do projeto, num lugar só. Todo ponto crítico (API de clima, MongoDB Atlas, RabbitMQ) usa esta classe.
 *
 * Funciona assim: executa a ação; se der erro, espera e tenta de novo. A espera dobra a cada tentativa
 * (backoff exponencial: 500 ms, 1 s, 2 s, 4 s...) até um teto, para dar tempo do serviço se recuperar
 * sem ficar martelando ele. Esgotadas as tentativas, lança RetryEsgotadoException e quem chamou decide o fallback.
 */
@Component
public class RetryExecutor {

    private static final Logger log = LoggerFactory.getLogger(RetryExecutor.class);
    private static final Duration ESPERA_MAXIMA = Duration.ofSeconds(15);

    /**
     * @param nome            aparece nos logs, ex: "API de clima (Manaus)"
     * @param tentativas      quantas vezes tentar no total
     * @param esperaInicial   espera depois da primeira falha
     * @param falhasSimuladas quantas das primeiras tentativas falham de propósito (modo caos, só para a apresentação)
     * @param acao            recebe o número da tentativa atual
     */
    public <T> T executar(String nome, int tentativas, Duration esperaInicial, int falhasSimuladas, IntFunction<T> acao) {
        RuntimeException ultimoErro = null;

        for (int tentativa = 1; tentativa <= tentativas; tentativa++) {
            try {
                if (tentativa <= falhasSimuladas) {
                    throw new IllegalStateException("falha simulada (modo caos)");
                }
                T resultado = acao.apply(tentativa);
                if (tentativa > 1) {
                    log.info("[retry] {}: deu certo na tentativa {}", nome, tentativa);
                }
                return resultado;
            } catch (RuntimeException erro) {
                ultimoErro = erro;
                if (tentativa == tentativas) {
                    break;
                }
                Duration espera = calcularEspera(esperaInicial, tentativa);
                log.warn("[retry] {}: tentativa {}/{} falhou ({}). Tentando de novo em {} ms...",
                        nome, tentativa, tentativas, erro.getMessage(), espera.toMillis());
                dormir(espera);
            }
        }

        log.error("[retry] {}: desisti depois de {} tentativas", nome, tentativas);
        throw new RetryEsgotadoException(nome, tentativas, ultimoErro);
    }

    public <T> T executar(String nome, int tentativas, Duration esperaInicial, IntFunction<T> acao) {
        return executar(nome, tentativas, esperaInicial, 0, acao);
    }

    public void executar(String nome, int tentativas, Duration esperaInicial, Runnable acao) {
        executar(nome, tentativas, esperaInicial, 0, tentativa -> {
            acao.run();
            return null;
        });
    }

    static Duration calcularEspera(Duration esperaInicial, int tentativa) {
        long millis = esperaInicial.toMillis() * (1L << (tentativa - 1));
        return Duration.ofMillis(Math.min(millis, ESPERA_MAXIMA.toMillis()));
    }

    private void dormir(Duration espera) {
        try {
            Thread.sleep(espera.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("retry interrompido", e);
        }
    }
}
