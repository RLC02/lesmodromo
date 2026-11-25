package com.lesmodromo.framework.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Frases da Rádio Lesma (coleção "narracoes").
 */
@Document("narracoes")
public record NarracaoDocument(@Id String id, String idCorrida, String texto, Instant criadaEm) {
}
