package com.lesmodromo.framework.controller.dto;

import com.lesmodromo.core.domain.Narracao;

import java.time.Instant;

public record NarracaoResponse(String idCorrida, String texto, Instant criadaEm) {

    public static NarracaoResponse de(Narracao narracao) {
        return new NarracaoResponse(narracao.idCorrida(), narracao.texto(), narracao.criadaEm());
    }
}
