package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Colocacao;
import com.lesmodromo.core.domain.Corrida;
import com.lesmodromo.core.domain.LeituraDoClima;
import com.lesmodromo.core.domain.Pista;
import com.lesmodromo.core.domain.Sorteio;
import com.lesmodromo.core.exception.CorridaNaoEncontradaException;
import com.lesmodromo.core.gateway.CorridaGateway;
import com.lesmodromo.core.gateway.EventoGateway;

import java.util.List;

/**
 * O juiz: consulta o clima, faz a corrida acontecer e publica "corrida.finalizada".
 * É chamado pelo consumidor da fila do juiz.
 */
public class ProcessarCorrida {

    private static final System.Logger LOG = System.getLogger(ProcessarCorrida.class.getName());

    private final CorridaGateway corridas;
    private final ConsultarClima consultarClima;
    private final EventoGateway eventos;
    private final AtualizarRanking atualizarRanking;
    private final Sorteio sorteio;

    public ProcessarCorrida(CorridaGateway corridas, ConsultarClima consultarClima, EventoGateway eventos,
                            AtualizarRanking atualizarRanking, Sorteio sorteio) {
        this.corridas = corridas;
        this.consultarClima = consultarClima;
        this.eventos = eventos;
        this.atualizarRanking = atualizarRanking;
        this.sorteio = sorteio;
    }

    public Corrida executar(String idCorrida) {
        Corrida corrida = corridas.buscarPorId(idCorrida).orElseThrow(() -> new CorridaNaoEncontradaException(idCorrida));
        if (corrida.finalizada()) {
            return corrida; // mensagem repetida: essa corrida já foi julgada
        }

        corrida.iniciar();
        corridas.salvar(corrida);

        // Clima com retry no gateway e fallback no ConsultarClima
        LeituraDoClima leitura = consultarClima.executar(corrida.getCidade(), corrida.isModoCaos());

        List<Colocacao> resultado = Pista.correr(leitura.clima(), sorteio);
        corrida.finalizar(leitura.clima(), leitura.tentativas(), leitura.fallback(), resultado);
        corridas.salvar(corrida);

        try {
            eventos.publicarCorridaFinalizada(corrida);
        } catch (RuntimeException erro) {
            // FALLBACK: sem mensageria, o ranking é atualizado direto para o jogador não perder os pontos.
            LOG.log(System.Logger.Level.WARNING, "[fallback] mensageria indisponível, ranking atualizado direto: {0}",
                    erro.getMessage());
            atualizarRanking.executar(corrida.getId());
        }
        return corrida;
    }
}
