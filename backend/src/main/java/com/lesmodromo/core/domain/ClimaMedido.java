package com.lesmodromo.core.domain;

/**
 * Clima devolvido pela API externa, junto com quantas tentativas foram precisas para obtê-lo.
 */
public record ClimaMedido(Clima clima, int tentativas) {
}
