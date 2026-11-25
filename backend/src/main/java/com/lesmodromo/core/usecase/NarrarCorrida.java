package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Corrida;
import com.lesmodromo.core.domain.Narracao;
import com.lesmodromo.core.domain.Narrador;
import com.lesmodromo.core.exception.CorridaNaoEncontradaException;
import com.lesmodromo.core.gateway.CorridaGateway;
import com.lesmodromo.core.gateway.NarracaoGateway;

import java.time.Instant;

/**
 * Cria a frase da Rádio Lesma. É chamado pelo consumidor da fila do narrador,
 * que recebe o MESMO evento "corrida.finalizada" que o ranking (pub/sub).
 */
public class NarrarCorrida {

    private final CorridaGateway corridas;
    private final NarracaoGateway narracoes;

    public NarrarCorrida(CorridaGateway corridas, NarracaoGateway narracoes) {
        this.corridas = corridas;
        this.narracoes = narracoes;
    }

    public void executar(String idCorrida) {
        Corrida corrida = corridas.buscarPorId(idCorrida).orElseThrow(() -> new CorridaNaoEncontradaException(idCorrida));
        if (!corrida.finalizada() || corrida.isNarrada()) {
            return;
        }
        narracoes.salvar(new Narracao(corrida.getId(), Narrador.narrar(corrida), Instant.now()));
        corridas.marcarNarrada(corrida.getId());
    }
}
