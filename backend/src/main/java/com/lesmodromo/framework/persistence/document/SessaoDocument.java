package com.lesmodromo.framework.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Sessões de login (coleção "sessoes"). O _id é o hash do token.
 * Um índice TTL em expiraEm faz o próprio MongoDB apagar as sessões vencidas.
 */
@Document("sessoes")
public record SessaoDocument(@Id String tokenHash, String usuarioId, Instant expiraEm) {
}
