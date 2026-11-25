package com.lesmodromo.core.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PistaTest {

    private static final Sorteio MEIO = () -> 0.5; // "aleatório" fixo para o teste ser previsível

    @Test
    void resultadoTemTodasAsLesmasDaMaisRapidaParaAMaisLenta() {
        List<Colocacao> resultado = Pista.correr(new Clima(20, 70, 10, 0), MEIO);

        assertEquals(Lesma.values().length, resultado.size());
        for (int i = 1; i < resultado.size(); i++) {
            assertTrue(resultado.get(i - 1).pontuacao() >= resultado.get(i).pontuacao());
            assertEquals(i + 1, resultado.get(i).posicao());
        }
    }

    @Test
    void gosminhaGanhaNoArMuitoUmido() {
        assertEquals(Lesma.GOSMINHA, vencedora(new Clima(30, 100, 0, 0)));
    }

    @Test
    void lesmaldaGanhaNoFrio() {
        assertEquals(Lesma.LESMALDA, vencedora(new Clima(2, 40, 0, 0)));
    }

    @Test
    void donaBabaGanhaNaVentania() {
        assertEquals(Lesma.DONA_BABA, vencedora(new Clima(30, 40, 40, 0)));
    }

    @Test
    void brasinhaGanhaNoCalorDeRachar() {
        assertEquals(Lesma.BRASINHA, vencedora(new Clima(38, 80, 25, 0)));
    }

    @Test
    void mestreZenGanhaNaCalmaria() {
        assertEquals(Lesma.MESTRE_ZEN, vencedora(new Clima(25, 60, 0, 0)));
    }

    @Test
    void garoaGanhaNaChuvaForte() {
        assertEquals(Lesma.GAROA, vencedora(new Clima(22, 60, 10, 5)));
    }

    @Test
    void baraoGanhaNoArSeco() {
        assertEquals(Lesma.BARAO, vencedora(new Clima(25, 20, 25, 0)));
    }

    @Test
    void cadaLesmaTemUmNumeroDiferenteDeUmAOito() {
        for (int i = 0; i < Lesma.values().length; i++) {
            assertEquals(i + 1, Lesma.values()[i].getNumero());
        }
    }

    @Test
    void vantagensVemDaMaiorParaAMenorComTodasAsLesmas() {
        List<Vantagem> vantagens = Pista.vantagens(new Clima(2, 40, 0, 0));

        assertEquals(Lesma.values().length, vantagens.size());
        assertEquals(Lesma.LESMALDA, vantagens.get(0).lesma());
        for (int i = 1; i < vantagens.size(); i++) {
            assertTrue(vantagens.get(i - 1).bonus() >= vantagens.get(i).bonus());
            assertEquals(i + 1, vantagens.get(i).posicao());
        }
    }

    @Test
    void naPrevisaoATurboMolengaEntraComASorteMedia() {
        Vantagem turbo = Pista.vantagens(new Clima(20, 60, 10, 0)).stream()
                .filter(v -> v.lesma() == Lesma.TURBO_MOLENGA)
                .findFirst()
                .orElseThrow();

        assertEquals(9.0, turbo.bonus(), 0.001);
    }

    @Test
    void chuvaDaBonusParaTodas() {
        double seco = bonusDa(Lesma.DONA_BABA, new Clima(30, 40, 0, 0));
        double molhado = bonusDa(Lesma.DONA_BABA, new Clima(30, 40, 0, 2));

        assertEquals(5.0, molhado - seco, 0.001);
    }

    private Lesma vencedora(Clima clima) {
        return Pista.correr(clima, MEIO).get(0).lesma();
    }

    private double bonusDa(Lesma lesma, Clima clima) {
        return Pista.correr(clima, MEIO).stream()
                .filter(c -> c.lesma() == lesma)
                .findFirst()
                .orElseThrow()
                .bonus();
    }
}
