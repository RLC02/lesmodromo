package com.lesmodromo.framework.controller;

import com.lesmodromo.core.domain.SessaoAberta;
import com.lesmodromo.core.usecase.CadastrarUsuario;
import com.lesmodromo.core.usecase.FazerLogin;
import com.lesmodromo.core.usecase.FazerLogout;
import com.lesmodromo.framework.controller.dto.CredenciaisRequest;
import com.lesmodromo.framework.controller.dto.UsuarioResponse;
import com.lesmodromo.framework.security.CookieDeSessao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login com sessão em cookie. O token nunca vai no corpo da resposta: só no cookie HttpOnly.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CadastrarUsuario cadastrarUsuario;
    private final FazerLogin fazerLogin;
    private final FazerLogout fazerLogout;
    private final CookieDeSessao cookieDeSessao;

    public AuthController(CadastrarUsuario cadastrarUsuario, FazerLogin fazerLogin, FazerLogout fazerLogout,
                          CookieDeSessao cookieDeSessao) {
        this.cadastrarUsuario = cadastrarUsuario;
        this.fazerLogin = fazerLogin;
        this.fazerLogout = fazerLogout;
        this.cookieDeSessao = cookieDeSessao;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody CredenciaisRequest credenciais, HttpServletRequest requisicao,
                                     HttpServletResponse resposta) {
        SessaoAberta sessao = cadastrarUsuario.executar(credenciais.nome(), credenciais.senha());
        cookieDeSessao.gravar(sessao, requisicao, resposta);
        return UsuarioResponse.de(sessao.usuario());
    }

    @PostMapping("/login")
    public UsuarioResponse entrar(@RequestBody CredenciaisRequest credenciais, HttpServletRequest requisicao,
                                  HttpServletResponse resposta) {
        SessaoAberta sessao = fazerLogin.executar(credenciais.nome(), credenciais.senha());
        cookieDeSessao.gravar(sessao, requisicao, resposta);
        return UsuarioResponse.de(sessao.usuario());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair(@CookieValue(name = CookieDeSessao.NOME, required = false) String token,
                     HttpServletRequest requisicao, HttpServletResponse resposta) {
        fazerLogout.executar(token);
        cookieDeSessao.apagar(requisicao, resposta);
    }

    /** O front pergunta aqui se tem alguém logado (ele não consegue ler o cookie HttpOnly). */
    @GetMapping("/eu")
    public UsuarioResponse eu(@CookieValue(name = CookieDeSessao.NOME, required = false) String token) {
        return UsuarioResponse.de(cookieDeSessao.exigirUsuario(token));
    }
}
