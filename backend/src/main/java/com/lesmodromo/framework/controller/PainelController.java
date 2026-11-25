package com.lesmodromo.framework.controller;

import com.lesmodromo.core.usecase.ConsultarRanking;
import com.lesmodromo.core.usecase.ListarNarracoes;
import com.lesmodromo.framework.controller.dto.InfoResponse;
import com.lesmodromo.framework.controller.dto.NarracaoResponse;
import com.lesmodromo.framework.controller.dto.RankingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PainelController {

    private final ConsultarRanking consultarRanking;
    private final ListarNarracoes listarNarracoes;

    public PainelController(ConsultarRanking consultarRanking, ListarNarracoes listarNarracoes) {
        this.consultarRanking = consultarRanking;
        this.listarNarracoes = listarNarracoes;
    }

    /** Lesmas e cidades. O front chama ao abrir (e é assim que ele "acorda" o Render). */
    @GetMapping("/info")
    public InfoResponse info() {
        return InfoResponse.atual();
    }

    @GetMapping("/ranking")
    public RankingResponse ranking() {
        return RankingResponse.de(consultarRanking.executar());
    }

    /** Rádio Lesma: últimas frases criadas pelo consumidor narrador. */
    @GetMapping("/narracoes")
    public List<NarracaoResponse> narracoes() {
        return listarNarracoes.executar().stream().map(NarracaoResponse::de).toList();
    }
}
