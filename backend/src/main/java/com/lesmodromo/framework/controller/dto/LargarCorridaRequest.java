package com.lesmodromo.framework.controller.dto;

import com.lesmodromo.core.usecase.PedidoDeCorrida;

/**
 * O nome do apostador não vem daqui: vem do usuário logado (cookie de sessão).
 */
public record LargarCorridaRequest(String lesma, String cidade, boolean modoCaos, String idCliente) {

    public PedidoDeCorrida paraPedido(String jogador) {
        return new PedidoDeCorrida(jogador, lesma, cidade, modoCaos, idCliente);
    }
}
