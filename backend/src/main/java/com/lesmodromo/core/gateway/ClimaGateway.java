package com.lesmodromo.core.gateway;

import com.lesmodromo.core.domain.Cidade;
import com.lesmodromo.core.domain.ClimaMedido;
import com.lesmodromo.core.exception.ClimaIndisponivelException;

/**
 * Porta de saída para a API externa de clima.
 */
public interface ClimaGateway {

    /**
     * @param modoCaos força falhas de propósito, para demonstrar o retry
     * @throws ClimaIndisponivelException se a API não responder depois de todas as tentativas
     */
    ClimaMedido medir(Cidade cidade, boolean modoCaos);
}
