package com.lesmodromo.core.domain;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Regra da corrida: pontuação = base aleatória (40 a 60) + bônus do clima (+5 para todas se estiver chovendo).
 * Quem fizer mais pontos ganha.
 */
public final class Pista {

    private static final double BASE = 40;
    private static final double VARIACAO = 20;
    private static final double BONUS_CHUVA = 5;

    private Pista() {
    }

    public static List<Colocacao> correr(Clima clima, Sorteio sorteio) {
        record Parcial(Lesma lesma, double pontuacao, double bonus) {
        }

        List<Parcial> ordenadas = Arrays.stream(Lesma.values())
                .map(lesma -> {
                    double bonus = lesma.bonus(clima, sorteio) + (clima.chovendo() ? BONUS_CHUVA : 0);
                    double pontuacao = BASE + sorteio.proximo() * VARIACAO + bonus;
                    return new Parcial(lesma, arredondar(pontuacao), arredondar(bonus));
                })
                .sorted(Comparator.comparingDouble(Parcial::pontuacao).reversed())
                .toList();

        return IntStream.range(0, ordenadas.size())
                .mapToObj(i -> new Colocacao(i + 1, ordenadas.get(i).lesma(), ordenadas.get(i).pontuacao(), ordenadas.get(i).bonus()))
                .toList();
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
