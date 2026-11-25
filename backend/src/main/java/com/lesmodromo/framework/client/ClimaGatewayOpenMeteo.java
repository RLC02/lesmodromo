package com.lesmodromo.framework.client;

import com.lesmodromo.core.domain.Cidade;
import com.lesmodromo.core.domain.Clima;
import com.lesmodromo.core.domain.ClimaMedido;
import com.lesmodromo.core.exception.ClimaIndisponivelException;
import com.lesmodromo.core.gateway.ClimaGateway;
import com.lesmodromo.framework.resilience.RetryEsgotadoException;
import com.lesmodromo.framework.resilience.RetryExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RETRY 1: consumo da API externa de clima (Open-Meteo, gratuita e sem chave).
 *
 * API externa a gente não controla: ela pode demorar, devolver 5xx ou cair por alguns segundos.
 * Aqui cada chamada tem timeout e até 4 tentativas. Se mesmo assim não der, lança
 * ClimaIndisponivelException e o caso de uso ProcessarCorrida usa o clima padrão (fallback).
 */
@Component
public class ClimaGatewayOpenMeteo implements ClimaGateway {

    private static final int TENTATIVAS = 4;
    private static final int FALHAS_NO_MODO_CAOS = 2;
    private static final String CAMPOS = "temperature_2m,relative_humidity_2m,wind_speed_10m,precipitation";

    private final RestClient restClient;
    private final RetryExecutor retry;

    public ClimaGatewayOpenMeteo(RestClient.Builder builder, RetryExecutor retry,
                                 @Value("${lesmodromo.clima-url}") String urlDaApi) {
        SimpleClientHttpRequestFactory timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(Duration.ofSeconds(3));
        timeouts.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = builder.baseUrl(urlDaApi).requestFactory(timeouts).build();
        this.retry = retry;
    }

    @Override
    public ClimaMedido medir(Cidade cidade, boolean modoCaos) {
        AtomicInteger tentativaQueFuncionou = new AtomicInteger();

        try {
            OpenMeteoResposta resposta = retry.executar(
                    "API de clima (" + cidade.getNome() + ")",
                    TENTATIVAS,
                    Duration.ofMillis(500),
                    modoCaos ? FALHAS_NO_MODO_CAOS : 0,
                    tentativa -> {
                        tentativaQueFuncionou.set(tentativa);
                        return buscar(cidade);
                    });

            OpenMeteoResposta.Atual atual = resposta.current();
            Clima clima = new Clima(atual.temperatura(), atual.umidade(), atual.vento(), atual.chuva());
            return new ClimaMedido(clima, tentativaQueFuncionou.get());
        } catch (RetryEsgotadoException erro) {
            throw new ClimaIndisponivelException(TENTATIVAS, erro);
        }
    }

    private OpenMeteoResposta buscar(Cidade cidade) {
        OpenMeteoResposta resposta = restClient.get()
                .uri(uri -> uri.path("/v1/forecast")
                        .queryParam("latitude", cidade.getLatitude())
                        .queryParam("longitude", cidade.getLongitude())
                        .queryParam("current", CAMPOS)
                        .build())
                .retrieve()
                .body(OpenMeteoResposta.class);

        if (resposta == null || resposta.current() == null) {
            throw new IllegalStateException("Open-Meteo respondeu sem o clima atual");
        }
        return resposta;
    }
}
