package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Sessao;
import com.lesmodromo.core.domain.SessaoAberta;
import com.lesmodromo.core.domain.Usuario;
import com.lesmodromo.core.exception.NaoAutenticadoException;
import com.lesmodromo.core.exception.RegraDeNegocioException;
import com.lesmodromo.core.gateway.SenhaGateway;
import com.lesmodromo.core.gateway.SessaoGateway;
import com.lesmodromo.core.gateway.UsuarioGateway;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginTest {

    /** Relógio que o teste consegue adiantar, para simular a sessão vencendo. */
    static class RelogioDeTeste extends Clock {
        Instant agora = Instant.parse("2026-11-25T20:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }

    static class Usuarios implements UsuarioGateway {
        final Map<String, Usuario> porId = new HashMap<>();

        @Override
        public Usuario salvar(Usuario usuario) {
            porId.put(usuario.id(), usuario);
            return usuario;
        }

        @Override
        public Optional<Usuario> buscarPorNome(String nome) {
            return porId.values().stream().filter(u -> Usuario.chave(u.nome()).equals(Usuario.chave(nome))).findFirst();
        }

        @Override
        public Optional<Usuario> buscarPorId(String id) {
            return Optional.ofNullable(porId.get(id));
        }
    }

    static class Sessoes implements SessaoGateway {
        final Map<String, Sessao> porHash = new HashMap<>();

        @Override
        public void salvar(Sessao sessao) {
            porHash.put(sessao.tokenHash(), sessao);
        }

        @Override
        public Optional<Sessao> buscar(String tokenHash) {
            return Optional.ofNullable(porHash.get(tokenHash));
        }

        @Override
        public void remover(String tokenHash) {
            porHash.remove(tokenHash);
        }
    }

    /** "Hash" de mentira, só para o teste. No sistema de verdade é BCrypt. */
    static class SenhaFalsa implements SenhaGateway {
        @Override
        public String gerarHash(String senha) {
            return "hash:" + senha;
        }

        @Override
        public boolean confere(String senha, String hash) {
            return hash.equals("hash:" + senha);
        }
    }

    private final RelogioDeTeste relogio = new RelogioDeTeste();
    private final Usuarios usuarios = new Usuarios();
    private final Sessoes sessoes = new Sessoes();
    private final SenhaFalsa senhas = new SenhaFalsa();
    private final AbrirSessao abrirSessao = new AbrirSessao(sessoes, relogio);
    private final CadastrarUsuario cadastrar = new CadastrarUsuario(usuarios, senhas, abrirSessao);
    private final FazerLogin login = new FazerLogin(usuarios, senhas, abrirSessao);
    private final FazerLogout logout = new FazerLogout(sessoes);
    private final IdentificarUsuario identificar = new IdentificarUsuario(sessoes, usuarios, relogio);

    @Test
    void cadastroCriaAContaEJaEntra() {
        SessaoAberta sessao = cadastrar.executar("Vini", "segredo123");

        assertNotNull(sessao.token());
        assertEquals("Vini", identificar.executar(sessao.token()).orElseThrow().nome());
    }

    @Test
    void senhaNuncaEGuardadaEmTextoPuro() {
        cadastrar.executar("Vini", "segredo123");

        Usuario salvo = usuarios.buscarPorNome("Vini").orElseThrow();
        assertFalse(salvo.senhaHash().equals("segredo123"));
    }

    @Test
    void bancoGuardaSoOHashDoToken() {
        SessaoAberta sessao = cadastrar.executar("Vini", "segredo123");

        assertFalse(sessoes.porHash.containsKey(sessao.token()));
        assertEquals(1, sessoes.porHash.size());
    }

    @Test
    void naoDeixaDoisUsuariosComOMesmoNome() {
        cadastrar.executar("Vini", "segredo123");

        assertThrows(RegraDeNegocioException.class, () -> cadastrar.executar("vini", "outrasenha"));
    }

    @Test
    void recusaNomeOuSenhaInvalidos() {
        assertThrows(RegraDeNegocioException.class, () -> cadastrar.executar("ab", "segredo123"));
        assertThrows(RegraDeNegocioException.class, () -> cadastrar.executar("nome com espaco", "segredo123"));
        assertThrows(RegraDeNegocioException.class, () -> cadastrar.executar("Vini", "123"));
    }

    @Test
    void loginComSenhaCertaEntra() {
        cadastrar.executar("Vini", "segredo123");

        SessaoAberta sessao = login.executar("vini", "segredo123");

        assertEquals("Vini", identificar.executar(sessao.token()).orElseThrow().nome());
    }

    @Test
    void loginComSenhaErradaOuNomeInexistenteFalhaComAMesmaMensagem() {
        cadastrar.executar("Vini", "segredo123");

        NaoAutenticadoException senhaErrada = assertThrows(NaoAutenticadoException.class, () -> login.executar("Vini", "errada"));
        NaoAutenticadoException semConta = assertThrows(NaoAutenticadoException.class, () -> login.executar("Ninguem", "segredo123"));
        assertEquals(senhaErrada.getMessage(), semConta.getMessage());
    }

    @Test
    void logoutFazOTokenPararDeValer() {
        SessaoAberta sessao = cadastrar.executar("Vini", "segredo123");

        logout.executar(sessao.token());

        assertTrue(identificar.executar(sessao.token()).isEmpty());
    }

    @Test
    void sessaoVencidaNaoValeMais() {
        SessaoAberta sessao = cadastrar.executar("Vini", "segredo123");

        relogio.agora = relogio.agora.plus(AbrirSessao.DURACAO).plus(Duration.ofSeconds(1));

        assertTrue(identificar.executar(sessao.token()).isEmpty());
        assertTrue(sessoes.porHash.isEmpty());
    }

    @Test
    void tokenInventadoNaoIdentificaNinguem() {
        cadastrar.executar("Vini", "segredo123");

        assertTrue(identificar.executar("token-inventado").isEmpty());
        assertTrue(identificar.executar(null).isEmpty());
    }
}
