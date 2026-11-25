package com.lesmodromo.framework.controller.dto;

import com.lesmodromo.core.domain.Clima;
import com.lesmodromo.core.domain.PrevisaoDoPareo;
import com.lesmodromo.framework.controller.dto.CorridaResponse.ClimaResponse;

import java.util.List;

public record PrevisaoResponse(
        String cidade,
        String nomeCidade,
        ClimaResponse clima,
        int tentativasClima,
        boolean climaFallback,
        List<VantagemResponse> vantagens) {

    public record VantagemResponse(int posicao, String lesma, String nome, String especialidade, double bonus) {
    }

    public static PrevisaoResponse de(PrevisaoDoPareo previsao) {
        Clima clima = previsao.leitura().clima();
        return new PrevisaoResponse(
                previsao.cidade().name(),
                previsao.cidade().getNome(),
                new ClimaResponse(clima.temperatura(), clima.umidade(), clima.vento(), clima.chuva()),
                previsao.leitura().tentativas(),
                previsao.leitura().fallback(),
                previsao.vantagens().stream()
                        .map(v -> new VantagemResponse(v.posicao(), v.lesma().name(), v.lesma().getNome(),
                                v.lesma().getEspecialidade(), v.bonus()))
                        .toList());
    }
}
