package com.lesmodromo.core.domain;

import java.time.Instant;

/**
 * Resultado de um login: o usuário e o token que vai para o cookie.
 */
public record SessaoAberta(Usuario usuario, String token, Instant expiraEm) {
}
