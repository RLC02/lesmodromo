package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Narracao;
import com.lesmodromo.core.gateway.NarracaoGateway;

import java.util.List;

public class ListarNarracoes {

    private static final int ULTIMAS = 8;

    private final NarracaoGateway narracoes;

    public ListarNarracoes(NarracaoGateway narracoes) {
        this.narracoes = narracoes;
    }

    public List<Narracao> executar() {
        return narracoes.ultimas(ULTIMAS);
    }
}
