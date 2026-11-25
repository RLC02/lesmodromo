package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Corrida;
import com.lesmodromo.core.exception.CorridaNaoEncontradaException;
import com.lesmodromo.core.gateway.CorridaGateway;

/**
 * O front chama isso de tempos em tempos para acompanhar o status da corrida.
 */
public class ConsultarCorrida {

    private final CorridaGateway corridas;

    public ConsultarCorrida(CorridaGateway corridas) {
        this.corridas = corridas;
    }

    public Corrida executar(String idCorrida) {
        return corridas.buscarPorId(idCorrida).orElseThrow(() -> new CorridaNaoEncontradaException(idCorrida));
    }
}
