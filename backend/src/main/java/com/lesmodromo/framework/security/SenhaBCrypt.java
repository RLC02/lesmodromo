package com.lesmodromo.framework.security;

import com.lesmodromo.core.gateway.SenhaGateway;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Hash de senha com BCrypt: lento de propósito e com "sal" aleatório,
 * então duas senhas iguais viram hashes diferentes e ataque por força bruta fica caro.
 */
@Component
public class SenhaBCrypt implements SenhaGateway {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String gerarHash(String senha) {
        return bcrypt.encode(senha);
    }

    @Override
    public boolean confere(String senha, String hash) {
        return bcrypt.matches(senha, hash);
    }
}
