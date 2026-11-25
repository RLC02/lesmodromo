package com.lesmodromo.core.domain;

import java.time.Instant;

/**
 * Sessão de login guardada no banco. O token em si fica só no cookie do navegador;
 * aqui guardamos o hash dele, então quem ler o banco não consegue usar a sessão.
 */
public record Sessao(String tokenHash, String usuarioId, Instant expiraEm) {

    public boolean expirada(Instant agora) {
        return !agora.isBefore(expiraEm);
    }
}
