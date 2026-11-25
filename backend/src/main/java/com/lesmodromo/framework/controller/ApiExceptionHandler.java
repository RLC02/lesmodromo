package com.lesmodromo.framework.controller;

import com.lesmodromo.core.exception.CorridaNaoEncontradaException;
import com.lesmodromo.core.exception.RegraDeNegocioException;
import com.lesmodromo.framework.controller.dto.ErroResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduz as exceções em respostas HTTP. 4xx = o front não deve repetir; 5xx = vale o retry do front.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RegraDeNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse regraDeNegocio(RegraDeNegocioException erro) {
        return new ErroResponse(erro.getMessage());
    }

    @ExceptionHandler(CorridaNaoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse naoEncontrada(CorridaNaoEncontradaException erro) {
        return new ErroResponse(erro.getMessage());
    }

    /** Endereço que não existe (ex: /healt em vez de /health). Antes caía no 500 genérico. */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse enderecoInexistente(NoResourceFoundException erro) {
        return new ErroResponse("Endereço não encontrado. Veja /health, /api/info e /api/ranking");
    }

    /** JSON mal formado no corpo da requisição. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse corpoInvalido(HttpMessageNotReadableException erro) {
        return new ErroResponse("Corpo da requisição inválido");
    }

    /** Ex: GET num endereço que só aceita POST. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErroResponse metodoNaoPermitido(HttpRequestMethodNotSupportedException erro) {
        return new ErroResponse("Método não permitido neste endereço");
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErroResponse bancoIndisponivel(DataAccessResourceFailureException erro) {
        log.error("[api] banco indisponível: {}", erro.getMessage());
        return new ErroResponse("Banco indisponível no momento, tente de novo");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroResponse inesperado(Exception erro) {
        log.error("[api] erro inesperado", erro);
        return new ErroResponse("Algo deu errado no servidor");
    }
}
