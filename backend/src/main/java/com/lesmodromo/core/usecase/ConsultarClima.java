package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Cidade;
import com.lesmodromo.core.domain.Clima;
import com.lesmodromo.core.domain.ClimaMedido;
import com.lesmodromo.core.domain.LeituraDoClima;
import com.lesmodromo.core.exception.ClimaIndisponivelException;
import com.lesmodromo.core.gateway.ClimaGateway;

/**
 * Busca o clima de uma cidade. Se a API não responder nem com o retry do gateway,
 * devolve o clima padrão (fallback). Usado pela corrida e pela previsão, para essa regra não ficar repetida.
 */
public class ConsultarClima {

    private static final System.Logger LOG = System.getLogger(ConsultarClima.class.getName());

    private final ClimaGateway climaGateway;

    public ConsultarClima(ClimaGateway climaGateway) {
        this.climaGateway = climaGateway;
    }

    public LeituraDoClima executar(Cidade cidade, boolean modoCaos) {
        try {
            ClimaMedido medido = climaGateway.medir(cidade, modoCaos);
            return new LeituraDoClima(medido.clima(), medido.tentativas(), false);
        } catch (ClimaIndisponivelException erro) {
            // FALLBACK: a API de clima não respondeu nem com retry. Segue com o clima padrão.
            LOG.log(System.Logger.Level.WARNING, "[fallback] usando clima padrão para {0}", cidade.getNome());
            return new LeituraDoClima(Clima.PADRAO, erro.getTentativas(), true);
        }
    }
}
