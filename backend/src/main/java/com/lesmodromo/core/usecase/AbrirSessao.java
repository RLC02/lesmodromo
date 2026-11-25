package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Sessao;
import com.lesmodromo.core.domain.SessaoAberta;
import com.lesmodromo.core.domain.TokenDeSessao;
import com.lesmodromo.core.domain.Usuario;
import com.lesmodromo.core.gateway.SessaoGateway;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Cria a sessão de um usuário. Usado pelo cadastro e pelo login, para não repetir essa regra.
 */
public class AbrirSessao {

    public static final Duration DURACAO = Duration.ofDays(7);

    private final SessaoGateway sessoes;
    private final Clock relogio;

    public AbrirSessao(SessaoGateway sessoes, Clock relogio) {
        this.sessoes = sessoes;
        this.relogio = relogio;
    }

    public SessaoAberta executar(Usuario usuario) {
        String token = TokenDeSessao.novo();
        Instant expiraEm = relogio.instant().plus(DURACAO);
        sessoes.salvar(new Sessao(TokenDeSessao.hash(token), usuario.id(), expiraEm));
        return new SessaoAberta(usuario, token, expiraEm);
    }
}
