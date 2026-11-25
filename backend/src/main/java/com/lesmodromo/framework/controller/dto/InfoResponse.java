package com.lesmodromo.framework.controller.dto;

import com.lesmodromo.core.domain.Cidade;
import com.lesmodromo.core.domain.Lesma;

import java.util.Arrays;
import java.util.List;

public record InfoResponse(List<LesmaResponse> lesmas, List<CidadeResponse> cidades) {

    public record LesmaResponse(String codigo, int numero, String nome, String especialidade, String descricao) {
    }

    public record CidadeResponse(String codigo, String nome) {
    }

    public static InfoResponse atual() {
        return new InfoResponse(
                Arrays.stream(Lesma.values())
                        .map(l -> new LesmaResponse(l.name(), l.getNumero(), l.getNome(), l.getEspecialidade(), l.getDescricao()))
                        .toList(),
                Arrays.stream(Cidade.values())
                        .map(c -> new CidadeResponse(c.name(), c.getNome()))
                        .toList());
    }
}
