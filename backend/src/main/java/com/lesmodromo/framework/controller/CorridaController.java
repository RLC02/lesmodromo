package com.lesmodromo.framework.controller;

import com.lesmodromo.core.usecase.ConsultarCorrida;
import com.lesmodromo.core.usecase.LargarCorrida;
import com.lesmodromo.framework.controller.dto.CorridaResponse;
import com.lesmodromo.framework.controller.dto.LargarCorridaRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/corridas")
public class CorridaController {

    private final LargarCorrida largarCorrida;
    private final ConsultarCorrida consultarCorrida;

    public CorridaController(LargarCorrida largarCorrida, ConsultarCorrida consultarCorrida) {
        this.largarCorrida = largarCorrida;
        this.consultarCorrida = consultarCorrida;
    }

    /**
     * Registra a aposta e devolve 202 (Accepted): a corrida ainda vai acontecer,
     * quando o juiz consumir a mensagem. O front acompanha pelo GET abaixo.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CorridaResponse largar(@RequestBody LargarCorridaRequest request) {
        return CorridaResponse.de(largarCorrida.executar(request.paraPedido()));
    }

    @GetMapping("/{id}")
    public CorridaResponse consultar(@PathVariable("id") String id) {
        return CorridaResponse.de(consultarCorrida.executar(id));
    }
}
