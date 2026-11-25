package com.lesmodromo.core.gateway;

import com.lesmodromo.core.domain.Narracao;

import java.util.List;

public interface NarracaoGateway {

    void salvar(Narracao narracao);

    List<Narracao> ultimas(int limite);
}
