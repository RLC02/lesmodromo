package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.SessaoAberta;
import com.lesmodromo.core.domain.Usuario;
import com.lesmodromo.core.exception.NaoAutenticadoException;
import com.lesmodromo.core.gateway.SenhaGateway;
import com.lesmodromo.core.gateway.UsuarioGateway;

/**
 * Confere nome e senha e abre uma sessão.
 * A mensagem de erro é a mesma para usuário inexistente e senha errada, para não revelar quais nomes existem.
 */
public class FazerLogin {

    private final UsuarioGateway usuarios;
    private final SenhaGateway senhas;
    private final AbrirSessao abrirSessao;

    public FazerLogin(UsuarioGateway usuarios, SenhaGateway senhas, AbrirSessao abrirSessao) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.abrirSessao = abrirSessao;
    }

    public SessaoAberta executar(String nome, String senha) {
        Usuario usuario = usuarios.buscarPorNome(nome == null ? "" : nome.trim())
                .filter(u -> senha != null && senhas.confere(senha, u.senhaHash()))
                .orElseThrow(() -> new NaoAutenticadoException("Nome ou senha incorretos"));
        return abrirSessao.executar(usuario);
    }
}
