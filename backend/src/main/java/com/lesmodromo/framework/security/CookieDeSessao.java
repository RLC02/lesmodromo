package com.lesmodromo.framework.security;

import com.lesmodromo.core.domain.SessaoAberta;
import com.lesmodromo.core.domain.Usuario;
import com.lesmodromo.core.exception.NaoAutenticadoException;
import com.lesmodromo.core.usecase.IdentificarUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Tudo sobre o cookie de login num lugar só.
 *
 * - HttpOnly: o JavaScript da página NÃO consegue ler o cookie (protege contra XSS).
 *   Por isso o front não guarda nada em localStorage: quem sabe se você está logado é o servidor.
 * - SameSite=Lax: o navegador não manda o cookie em POST vindo de outro site (protege contra CSRF).
 * - Secure: em produção (https) o cookie só trafega criptografado.
 */
@Component
public class CookieDeSessao {

    public static final String NOME = "lesmodromo_sessao";

    private final IdentificarUsuario identificarUsuario;

    public CookieDeSessao(IdentificarUsuario identificarUsuario) {
        this.identificarUsuario = identificarUsuario;
    }

    public void gravar(SessaoAberta sessao, HttpServletRequest requisicao, HttpServletResponse resposta) {
        Duration validade = Duration.between(Instant.now(), sessao.expiraEm());
        resposta.addHeader(HttpHeaders.SET_COOKIE, montar(sessao.token(), validade, requisicao).toString());
    }

    public void apagar(HttpServletRequest requisicao, HttpServletResponse resposta) {
        resposta.addHeader(HttpHeaders.SET_COOKIE, montar("", Duration.ZERO, requisicao).toString());
    }

    /** Usado nos endpoints que exigem login. Sem sessão válida, vira HTTP 401. */
    public Usuario exigirUsuario(String token) {
        return identificarUsuario.executar(token)
                .orElseThrow(() -> new NaoAutenticadoException("Entre na sua conta para continuar"));
    }

    private ResponseCookie montar(String valor, Duration validade, HttpServletRequest requisicao) {
        return ResponseCookie.from(NOME, valor)
                .httpOnly(true)
                .secure(requisicao.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(validade)
                .build();
    }
}
