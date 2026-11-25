package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Cidade;
import com.lesmodromo.core.domain.Corrida;
import com.lesmodromo.core.domain.Lesma;
import com.lesmodromo.core.gateway.CorridaGateway;
import com.lesmodromo.core.gateway.EventoGateway;

import java.util.Optional;

/**
 * Registra a aposta e publica o evento "corrida.largada". A corrida em si acontece depois,
 * quando o juiz consumir essa mensagem.
 */
public class LargarCorrida {

    private static final System.Logger LOG = System.getLogger(LargarCorrida.class.getName());

    private final CorridaGateway corridas;
    private final EventoGateway eventos;
    private final ProcessarCorrida processarCorrida;

    public LargarCorrida(CorridaGateway corridas, EventoGateway eventos, ProcessarCorrida processarCorrida) {
        this.corridas = corridas;
        this.eventos = eventos;
        this.processarCorrida = processarCorrida;
    }

    public Corrida executar(PedidoDeCorrida pedido) {
        if (pedido.idCliente() != null && !pedido.idCliente().isBlank()) {
            Optional<Corrida> existente = corridas.buscarPorIdCliente(pedido.idCliente());
            if (existente.isPresent()) {
                return existente.get(); // o front repetiu o pedido (retry): devolve a mesma corrida
            }
        }

        Lesma lesma = Lesma.porCodigo(pedido.lesma());
        Cidade cidade = Cidade.porCodigo(pedido.cidade());
        Corrida corrida = corridas.salvar(Corrida.nova(pedido.idCliente(), pedido.jogador(), lesma, cidade, pedido.modoCaos()));

        try {
            eventos.publicarCorridaLargada(corrida);
            return corrida;
        } catch (RuntimeException erro) {
            // FALLBACK: a mensageria não respondeu nem com retry. Em vez de perder a aposta, o juiz roda aqui mesmo.
            LOG.log(System.Logger.Level.WARNING, "[fallback] mensageria indisponível, corrida {0} processada direto: {1}",
                    corrida.getId(), erro.getMessage());
            return processarCorrida.executar(corrida.getId());
        }
    }
}
