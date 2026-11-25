package com.lesmodromo.core.domain;

import java.time.Instant;

/**
 * Uma frase da Rádio Lesma, criada pelo consumidor "narrador".
 */
public record Narracao(String idCorrida, String texto, Instant criadaEm) {
}
