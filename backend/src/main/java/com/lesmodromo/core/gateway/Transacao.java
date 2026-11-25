package com.lesmodromo.core.gateway;

/**
 * Porta para executar várias gravações como uma coisa só: ou grava tudo, ou não grava nada.
 * O core só diz ONDE a transação começa e termina; COMO ela é feita (MongoDB) fica na camada framework.
 */
public interface Transacao {

    void executar(Runnable acao);
}
