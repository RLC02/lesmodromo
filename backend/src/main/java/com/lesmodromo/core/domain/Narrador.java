package com.lesmodromo.core.domain;

import java.util.Locale;

/**
 * Transforma o resultado de uma corrida numa frase de locutor para a "Rádio Lesma".
 */
public final class Narrador {

    private static final Locale BRASIL = Locale.of("pt", "BR");

    private Narrador() {
    }

    public static String narrar(Corrida corrida) {
        Lesma vencedora = corrida.vencedora();
        Clima clima = corrida.getClima();
        String cidade = corrida.getCidade().getNome();

        String lance = switch (vencedora) {
            case GOSMINHA -> String.format(BRASIL, "aproveitou os %.0f%% de umidade", clima.umidade());
            case LESMALDA -> String.format(BRASIL, "se animou com os %.0f °C", clima.temperatura());
            case DONA_BABA -> String.format(BRASIL, "pegou carona no vento de %.0f km/h", clima.vento());
            case TURBO_MOLENGA -> "acordou inspirado e foi na pura sorte";
            case BRASINHA -> String.format(BRASIL, "esquentou os motores com %.0f °C", clima.temperatura());
            case MESTRE_ZEN -> String.format(BRASIL, "meditou no vento fraquinho de %.0f km/h", clima.vento());
            case GAROA -> clima.chovendo()
                    ? String.format(BRASIL, "abriu o guarda-chuva com %.1f mm de chuva", clima.chuva())
                    : "abriu o guarda-chuva mesmo sem chuva e foi no embalo";
            case BARAO -> String.format(BRASIL, "desfilou no ar seco de %.0f%% de umidade", clima.umidade());
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
