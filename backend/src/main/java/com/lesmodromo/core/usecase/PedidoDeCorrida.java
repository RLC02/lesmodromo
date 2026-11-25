package com.lesmodromo.core.usecase;

/**
 * Dados de entrada para largar uma corrida.
 *
 * @param idCliente id gerado pelo navegador; se o front repetir o pedido (retry), a corrida não é duplicada
 */
public record PedidoDeCorrida(String jogador, String lesma, String cidade, boolean modoCaos, String idCliente) {
}
