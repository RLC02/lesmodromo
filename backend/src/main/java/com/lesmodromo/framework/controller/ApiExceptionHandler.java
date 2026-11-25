package com.lesmodromo.framework.controller;

import com.lesmodromo.core.exception.CorridaNaoEncontradaException;
import com.lesmodromo.core.exception.RegraDeNegocioException;
import com.lesmodromo.framework.controller.dto.ErroResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
