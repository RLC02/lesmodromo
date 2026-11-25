package com.lesmodromo.core.gateway;

/**
 * Como a senha é transformada em hash fica na camada framework (BCrypt). O core só pede.
 */
public interface SenhaGateway {

    String gerarHash(String senha);

    boolean confere(String senha, String hash);
}
