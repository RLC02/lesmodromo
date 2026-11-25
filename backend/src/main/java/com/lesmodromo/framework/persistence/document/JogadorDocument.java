package com.lesmodromo.framework.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Linha do ranking (coleção "jogadores").
 */
@Document("jogadores")
public record JogadorDocument(@Id String id, String nome, int pontos, int corridas, int vitorias) {
}
