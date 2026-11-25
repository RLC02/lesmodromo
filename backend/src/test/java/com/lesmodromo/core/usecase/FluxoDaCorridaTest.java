package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Clima;
import com.lesmodromo.core.domain.Corrida;
import com.lesmodromo.core.domain.Jogador;
import com.lesmodromo.core.domain.StatusCorrida;
import com.lesmodromo.core.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluxoDaCorridaTest {

    private final Fakes.Corridas corridas = new Fakes.Corridas();
    private final Fakes.Eventos eventos = new Fakes.Eventos();
    private final Fakes.ClimaFixo clima = new Fakes.ClimaFixo();
    private final Fakes.RankingEmMemoria ranking = new Fakes.RankingEmMemoria();
    private final Fakes.Narracoes narracoes = new Fakes.Narracoes();
    private final Fakes.TransacaoDireta transacao = new Fakes.TransacaoDireta();

    private final AtualizarRanking atualizarRanking = new AtualizarRanking(corridas, ranking, transacao);
    private final ProcessarCorrida processarCorrida =
            new ProcessarCorrida(corridas, clima, eventos, atualizarRanking, () -> 0.5);
    private final LargarCorrida largarCorrida = new LargarCorrida(corridas, eventos, processarCorrida);
    private final NarrarCorrida narrarCorrida = new NarrarCorrida(corridas, narracoes);

    private PedidoDeCorrida pedido(String idCliente) {
        return new PedidoDeCorrida("Vini", "GOSMINHA", "MANAUS", false, idCliente);
    }

    @Test
    void largarCorridaSalvaNaLargadaEPublicaOEvento() {
        Corrida corrida = largarCorrida.executar(pedido("a1"));

        assertEquals(StatusCorrida.NA_LARGADA, corrida.getStatus());
        assertEquals(List.of("corrida.largada"), eventos.publicados);
    }

    @Test
    void pedidoRepetidoPeloRetryDoFrontNaoDuplicaACorrida() {
        Corrida primeira = largarCorrida.executar(pedido("mesmo-id"));
        Corrida repetida = largarCorrida.executar(pedido("mesmo-id"));

        assertSame(primeira, repetida);
        assertEquals(1, corridas.porId.size());
        assertEquals(1, eventos.publicados.size());
    }

    @Test
    void juizFinalizaACorridaEPublicaCorridaFinalizada() {
        Corrida corrida = largarCorrida.executar(pedido("a2"));

        Corrida julgada = processarCorrida.executar(corrida.getId());

        assertEquals(StatusCorrida.FINALIZADA, julgada.getStatus());
        assertEquals(4, julgada.getResultado().size());
        assertEquals(List.of("corrida.largada", "corrida.finalizada"), eventos.publicados);
    }

    @Test
    void semApiDeClimaACorridaUsaClimaPadrao() {
        clima.foraDoAr = true;
        Corrida corrida = largarCorrida.executar(pedido("a3"));

        Corrida julgada = processarCorrida.executar(corrida.getId());

        assertTrue(julgada.isClimaFallback());
        assertEquals(Clima.PADRAO, julgada.getClima());
        assertEquals(4, julgada.getTentativasClima());
    }

    @Test
    void semMensageriaOJuizRodaDiretoEORankingTambem() {
        eventos.foraDoAr = true;

        Corrida corrida = largarCorrida.executar(pedido("a4"));

        assertEquals(StatusCorrida.FINALIZADA, corrida.getStatus());
        assertTrue(corridas.rankingMarcado.contains(corrida.getId()));
        assertEquals(1, ranking.jogadores.get("Vini").corridas());
    }

    @Test
    void rankingNaoContaAMesmaCorridaDuasVezes() {
        Corrida corrida = largarCorrida.executar(pedido("a5"));
        processarCorrida.executar(corrida.getId());

        atualizarRanking.executar(corrida.getId());
        atualizarRanking.executar(corrida.getId()); // mensagem entregue de novo

        Jogador vini = ranking.jogadores.get("Vini");
        assertEquals(1, vini.corridas());
        assertEquals(2, transacao.vezes);
    }

    @Test
    void rankingRecusaCorridaQueAindaNaoTerminou() {
        Corrida corrida = largarCorrida.executar(pedido("a6"));

        assertThrows(RegraDeNegocioException.class, () -> atualizarRanking.executar(corrida.getId()));
    }

    @Test
    void narradorCriaUmaFraseSoUmaVez() {
        Corrida corrida = largarCorrida.executar(pedido("a7"));
        processarCorrida.executar(corrida.getId());

        narrarCorrida.executar(corrida.getId());

        assertEquals(1, narracoes.salvas.size());
        assertTrue(narracoes.salvas.get(0).texto().contains("Manaus"));
        assertTrue(corridas.narradas.contains(corrida.getId()));
    }

    @Test
    void naoAceitaApostaSemNome() {
        PedidoDeCorrida semNome = new PedidoDeCorrida("  ", "GOSMINHA", "MANAUS", false, "a8");

        assertThrows(RegraDeNegocioException.class, () -> largarCorrida.executar(semNome));
        assertFalse(eventos.publicados.contains("corrida.largada"));
    }

    @Test
    void naoAceitaLesmaQueNaoExiste() {
        PedidoDeCorrida invalido = new PedidoDeCorrida("Vini", "CARACOL_TURBO", "MANAUS", false, "a9");

        assertThrows(RegraDeNegocioException.class, () -> largarCorrida.executar(invalido));
    }
}
