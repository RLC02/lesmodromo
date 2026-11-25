package com.lesmodromo.framework.controller.dto;

import com.lesmodromo.core.usecase.PedidoDeCorrida;

public record LargarCorridaRequest(String jogador, String lesma, String cidade, boolean modoCaos, String idCliente) {

    public PedidoDeCorrida paraPedido() {
        return new PedidoDeCorrida(jogador, lesma, cidade, modoCaos, idCliente);
    }
}
