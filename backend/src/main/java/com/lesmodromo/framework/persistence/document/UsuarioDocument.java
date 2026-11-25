package com.lesmodromo.framework.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Usuários (coleção "usuarios"). nomeChave é o nome em minúsculas, com índice único.
 */
@Document("usuarios")
public record UsuarioDocument(@Id String id, String nome, String nomeChave, String senhaHash, Instant criadoEm) {
}
