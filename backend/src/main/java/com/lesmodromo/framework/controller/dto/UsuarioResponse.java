package com.lesmodromo.framework.controller.dto;

import com.lesmodromo.core.domain.Usuario;

/**
 * O que o front recebe sobre o usuário logado. Nunca inclui senha nem token.
 */
public record UsuarioResponse(String nome) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.nome());
    }
}
