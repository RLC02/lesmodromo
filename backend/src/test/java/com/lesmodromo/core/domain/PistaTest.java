package com.lesmodromo.core.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PistaTest {

    private static final Sorteio MEIO = () -> 0.5; // "aleatório" fixo para o teste ser previsível

    @Test
    void resultadoTemAsQuatroLesmasDaMaisRapidaParaAMaisLenta() {
        List<Colocacao> resultado = Pista.correr(new Clima(20, 70, 10, 0), MEIO);

        assertEquals(4, resultado.size());
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
