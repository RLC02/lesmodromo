package com.lesmodromo.core.gateway;

import com.lesmodromo.core.domain.Jogador;
import com.lesmodromo.core.domain.VitoriasDaLesma;

import java.util.List;

public interface RankingGateway {

    void registrar(String jogador, int pontos, boolean venceu);

    List<Jogador> melhores(int limite);

    List<VitoriasDaLesma> vitoriasPorLesma();
}
