package com.lesmodromo.core.gateway;

import com.lesmodromo.core.domain.Corrida;

import java.util.Optional;

/**
 * Porta de saída para guardar corridas. Quem implementa é a camada framework (MongoDB).
 */
public interface CorridaGateway {

    Corrida salvar(Corrida corrida);

    Optional<Corrida> buscarPorId(String id);

    Optional<Corrida> buscarPorIdCliente(String idCliente);

    /**
     * Marca que o ranking já contou esta corrida.
     *
     * @return true se marcou agora; false se já estava marcada (mensagem repetida)
     */
    boolean marcarRankingAtualizado(String id);

    void marcarNarrada(String id);
}
