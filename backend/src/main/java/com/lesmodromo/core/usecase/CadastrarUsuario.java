package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.SessaoAberta;
import com.lesmodromo.core.domain.Usuario;
import com.lesmodromo.core.exception.RegraDeNegocioException;
import com.lesmodromo.core.gateway.SenhaGateway;
import com.lesmodromo.core.gateway.UsuarioGateway;

import java.util.UUID;

/**
 * Cria a conta e já entra nela.
 */
public class CadastrarUsuario {

    private static final String NOME_VALIDO = "[\\p{L}0-9_]{3,20}";
    private static final int SENHA_MINIMA = 6;

    private final UsuarioGateway usuarios;
    private final SenhaGateway senhas;
    private final AbrirSessao abrirSessao;

    public CadastrarUsuario(UsuarioGateway usuarios, SenhaGateway senhas, AbrirSessao abrirSessao) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.abrirSessao = abrirSessao;
    }

    public SessaoAberta executar(String nome, String senha) {
        String nomeLimpo = nome == null ? "" : nome.trim();
        if (!nomeLimpo.matches(NOME_VALIDO)) {
            throw new RegraDeNegocioException("O nome precisa ter de 3 a 20 letras, números ou _ (sem espaço)");
        }
        if (senha == null || senha.length() < SENHA_MINIMA) {
            throw new RegraDeNegocioException("A senha precisa ter pelo menos " + SENHA_MINIMA + " caracteres");
        }
        if (usuarios.buscarPorNome(nomeLimpo).isPresent()) {
            throw new RegraDeNegocioException("Esse nome já está em uso. Escolha outro ou entre na sua conta");
        }

        Usuario usuario = usuarios.salvar(new Usuario(UUID.randomUUID().toString(), nomeLimpo, senhas.gerarHash(senha)));
        return abrirSessao.executar(usuario);
    }
}
