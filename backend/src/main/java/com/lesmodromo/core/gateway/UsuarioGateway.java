package com.lesmodromo.core.gateway;

import com.lesmodromo.core.domain.Usuario;

import java.util.Optional;

public interface UsuarioGateway {

    Usuario salvar(Usuario usuario);

    /** Busca sem diferenciar maiúsculas de minúsculas. */
    Optional<Usuario> buscarPorNome(String nome);

    Optional<Usuario> buscarPorId(String id);
}
