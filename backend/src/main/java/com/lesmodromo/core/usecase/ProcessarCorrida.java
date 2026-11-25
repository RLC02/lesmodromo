package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Clima;
import com.lesmodromo.core.domain.ClimaMedido;
import com.lesmodromo.core.domain.Colocacao;
import com.lesmodromo.core.domain.Corrida;
import com.lesmodromo.core.domain.Pista;
import com.lesmodromo.core.domain.Sorteio;
import com.lesmodromo.core.exception.ClimaIndisponivelException;
import com.lesmodromo.core.exception.CorridaNaoEncontradaException;
import com.lesmodromo.core.gateway.ClimaGateway;
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
    private final ClimaGateway climaGateway;
    private final EventoGateway eventos;
    private final AtualizarRanking atualizarRanking;
    private final Sorteio sorteio;

    public ProcessarCorrida(CorridaGateway corridas, ClimaGateway climaGateway, EventoGateway eventos,
                            AtualizarRanking atualizarRanking, Sorteio sorteio) {
        this.corridas = corridas;
        this.climaGateway = climaGateway;
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

        ClimaMedido medido;
        boolean fallback = false;
        try {
            medido = climaGateway.medir(corrida.getCidade(), corrida.isModoCaos());
        } catch (ClimaIndisponivelException erro) {
            // FALLBACK: a API de clima não respondeu nem com retry. A corrida acontece com clima padrão.
            LOG.log(System.Logger.Level.WARNING, "[fallback] usando clima padrão para {0}", corrida.getCidade().getNome());
            medido = new ClimaMedido(Clima.PADRAO, erro.getTentativas());
            fallback = true;
        }

        List<Colocacao> resultado = Pista.correr(medido.clima(), sorteio);
        corrida.finalizar(medido.clima(), medido.tentativas(), fallback, resultado);
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
