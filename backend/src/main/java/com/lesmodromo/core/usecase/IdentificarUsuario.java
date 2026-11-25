package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Sessao;
import com.lesmodromo.core.domain.TokenDeSessao;
import com.lesmodromo.core.domain.Usuario;
import com.lesmodromo.core.gateway.SessaoGateway;
import com.lesmodromo.core.gateway.UsuarioGateway;

import java.time.Clock;
import java.util.Optional;

/**
 * Descobre quem é o usuário a partir do token do cookie. Sessão vencida não vale.
 */
public class IdentificarUsuario {

    private final SessaoGateway sessoes;
    private final UsuarioGateway usuarios;
    private final Clock relogio;

    public IdentificarUsuario(SessaoGateway sessoes, UsuarioGateway usuarios, Clock relogio) {
        this.sessoes = sessoes;
        this.usuarios = usuarios;
        this.relogio = relogio;
    }

    public Optional<Usuario> executar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String hash = TokenDeSessao.hash(token);
        Optional<Sessao> sessao = sessoes.buscar(hash);
        if (sessao.isEmpty()) {
            return Optional.empty();
        }
        if (sessao.get().expirada(relogio.instant())) {
            sessoes.remover(hash);
            return Optional.empty();
        }
        return usuarios.buscarPorId(sessao.get().usuarioId());
    }
}
