package com.lesmodromo.framework.config;

import com.lesmodromo.core.domain.Sorteio;
import com.lesmodromo.core.gateway.ClimaGateway;
import com.lesmodromo.core.gateway.CorridaGateway;
import com.lesmodromo.core.gateway.EventoGateway;
import com.lesmodromo.core.gateway.NarracaoGateway;
import com.lesmodromo.core.gateway.RankingGateway;
import com.lesmodromo.core.gateway.Transacao;
import com.lesmodromo.core.usecase.AtualizarRanking;
import com.lesmodromo.core.usecase.ConsultarClima;
import com.lesmodromo.core.usecase.ConsultarCorrida;
import com.lesmodromo.core.usecase.ConsultarRanking;
import com.lesmodromo.core.usecase.LargarCorrida;
import com.lesmodromo.core.usecase.ListarNarracoes;
import com.lesmodromo.core.usecase.NarrarCorrida;
import com.lesmodromo.core.usecase.PreverPareo;
import com.lesmodromo.core.usecase.ProcessarCorrida;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Aqui o Spring "monta" os casos de uso do core, entregando para cada um as implementações dos gateways.
 * É o único lugar onde core e framework se encontram. O core continua sem saber que o Spring existe.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public Sorteio sorteio() {
        return () -> ThreadLocalRandom.current().nextDouble();
    }

    @Bean
    public AtualizarRanking atualizarRanking(CorridaGateway corridas, RankingGateway ranking, Transacao transacao) {
        return new AtualizarRanking(corridas, ranking, transacao);
    }

    @Bean
    public ConsultarClima consultarClima(ClimaGateway climaGateway) {
        return new ConsultarClima(climaGateway);
    }

    @Bean
    public PreverPareo preverPareo(ConsultarClima consultarClima) {
        return new PreverPareo(consultarClima);
    }

    @Bean
    public ProcessarCorrida processarCorrida(CorridaGateway corridas, ConsultarClima consultarClima, EventoGateway eventos,
                                             AtualizarRanking atualizarRanking, Sorteio sorteio) {
        return new ProcessarCorrida(corridas, consultarClima, eventos, atualizarRanking, sorteio);
    }

    @Bean
    public LargarCorrida largarCorrida(CorridaGateway corridas, EventoGateway eventos, ProcessarCorrida processarCorrida) {
        return new LargarCorrida(corridas, eventos, processarCorrida);
    }

    @Bean
    public NarrarCorrida narrarCorrida(CorridaGateway corridas, NarracaoGateway narracoes) {
        return new NarrarCorrida(corridas, narracoes);
    }

    @Bean
    public ConsultarCorrida consultarCorrida(CorridaGateway corridas) {
        return new ConsultarCorrida(corridas);
    }

    @Bean
    public ConsultarRanking consultarRanking(RankingGateway ranking) {
        return new ConsultarRanking(ranking);
    }

    @Bean
    public ListarNarracoes listarNarracoes(NarracaoGateway narracoes) {
        return new ListarNarracoes(narracoes);
    }
}
