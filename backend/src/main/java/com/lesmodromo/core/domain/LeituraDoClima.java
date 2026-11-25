package com.lesmodromo.core.domain;

/**
 * Clima usado na corrida ou na previsão.
 *
 * @param fallback true quando a API não respondeu e foi usado o clima padrão
 */
public record LeituraDoClima(Clima clima, int tentativas, boolean fallback) {
}
