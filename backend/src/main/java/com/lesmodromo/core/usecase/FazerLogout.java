package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.TokenDeSessao;
import com.lesmodromo.core.gateway.SessaoGateway;

/**
 * Apaga a sessão no banco. Mesmo que alguém tenha copiado o cookie, ele para de valer.
 */
public class FazerLogout {

    private final SessaoGateway sessoes;

    public FazerLogout(SessaoGateway sessoes) {
        this.sessoes = sessoes;
    }

    public void executar(String token) {
        if (token != null && !token.isBlank()) {
            sessoes.remover(TokenDeSessao.hash(token));
        }
    }
}
