package com.lesmodromo.core.domain;

import java.util.Locale;

/**
 * Transforma o resultado de uma corrida numa frase de locutor para a "Rádio Lesma".
 */
public final class Narrador {

    private Narrador() {
    }

    public static String narrar(Corrida corrida) {
        Lesma vencedora = corrida.vencedora();
        Clima clima = corrida.getClima();
        String cidade = corrida.getCidade().getNome();

        String lance = switch (vencedora) {
            case GOSMINHA -> String.format(Locale.ROOT, "aproveitou os %.0f%% de umidade", clima.umidade());
            case LESMALDA -> String.format(Locale.ROOT, "se animou com os %.0f °C", clima.temperatura());
            case DONA_BABA -> String.format(Locale.ROOT, "pegou carona no vento de %.0f km/h", clima.vento());
            case TURBO_MOLENGA -> "acordou inspirado e foi na pura sorte";
        };

        String abertura = vencedora.getNome() + " " + lance + " em " + cidade + " e cruzou a linha em primeiro!";

        String aposta = corrida.venceu()
                ? " " + corrida.getJogador() + " apostou certo e leva " + Corrida.PONTOS_VITORIA + " pontos."
                : " " + corrida.getJogador() + " foi de " + corrida.getLesmaEscolhida().getNome()
                  + ", que chegou em " + corrida.posicaoDaLesmaEscolhida() + "º.";

        String chuva = clima.chovendo() ? " Pista molhada, festa das lesmas." : "";
        String fallback = corrida.isClimaFallback() ? " (A estação do tempo não respondeu, corremos com clima padrão.)" : "";

        return abertura + aposta + chuva + fallback;
    }
}
