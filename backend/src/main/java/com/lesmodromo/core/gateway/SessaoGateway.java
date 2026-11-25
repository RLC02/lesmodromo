package com.lesmodromo.core.gateway;

import com.lesmodromo.core.domain.Sessao;

import java.util.Optional;

public interface SessaoGateway {

    void salvar(Sessao sessao);

    Optional<Sessao> buscar(String tokenHash);

    void remover(String tokenHash);
}
