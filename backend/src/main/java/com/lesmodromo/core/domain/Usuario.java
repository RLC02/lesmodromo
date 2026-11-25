package com.lesmodromo.core.domain;

import java.util.Locale;

/**
 * Quem aposta. A senha nunca é guardada: só o hash dela.
 */
public record Usuario(String id, String nome, String senhaHash) {

    /** Nome em minúsculas, para "Vini" e "vini" serem o mesmo usuário. */
    public static String chave(String nome) {
        return nome == null ? "" : nome.trim().toLowerCase(Locale.ROOT);
    }
}
