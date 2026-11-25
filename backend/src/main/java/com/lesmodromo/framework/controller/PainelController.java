package com.lesmodromo.framework.controller;

import com.lesmodromo.core.usecase.ConsultarRanking;
import com.lesmodromo.core.usecase.ListarNarracoes;
import com.lesmodromo.core.usecase.PreverPareo;
import com.lesmodromo.framework.controller.dto.InfoResponse;
import com.lesmodromo.framework.controller.dto.NarracaoResponse;
import com.lesmodromo.framework.controller.dto.PrevisaoResponse;
import com.lesmodromo.framework.controller.dto.RankingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PainelController {

    private final ConsultarRanking consultarRanking;
    private final ListarNarracoes listarNarracoes;
    private final PreverPareo preverPareo;

    public PainelController(ConsultarRanking consultarRanking, ListarNarracoes listarNarracoes, PreverPareo preverPareo) {
        this.consultarRanking = consultarRanking;
        this.listarNarracoes = listarNarracoes;
        this.preverPareo = preverPareo;
    }

    /** Clima de agora na cidade e a vantagem de cada lesma. O front chama ao trocar de cidade. */
    @GetMapping("/cidades/{codigo}/clima")
    public PrevisaoResponse previsao(@PathVariable("codigo") String codigo) {
        return PrevisaoResponse.de(preverPareo.executar(codigo));
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
