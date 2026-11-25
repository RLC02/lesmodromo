package com.lesmodromo.core.domain;

import com.lesmodromo.core.exception.RegraDeNegocioException;

/**
 * As oito competidoras. Cada uma tem um ponto forte ligado ao clima,
 * então cada tipo de tempo favorece alguém diferente.
 */
public enum Lesma {

    GOSMINHA(1, "Gosminha", "Umidade", "Quanto mais úmido o ar, mais rápido ela desliza."),
    LESMALDA(2, "Lesmalda", "Frio", "Fica elétrica no frio. No calor, só quer dormir."),
    DONA_BABA(3, "Dona Baba", "Vento", "Abre a concha feito vela e pega carona no vento."),
    TURBO_MOLENGA(4, "Turbo Molenga", "Sorte", "Ignora o clima. Às vezes voa, às vezes cochila."),
    BRASINHA(5, "Brasinha", "Calor", "Adora um solzão. Quanto mais quente, mais acelera."),
    MESTRE_ZEN(6, "Mestre Zen", "Calmaria", "Medita melhor sem vento. Na calmaria, vai longe."),
    GAROA(7, "Garoa", "Chuva", "Não sai sem guarda-chuva. Se chover, ninguém segura."),
    BARAO(8, "Barão Babão", "Ar seco", "Nobre e exigente: só rende com o ar bem seco.");

    private final int numero;
    private final String nome;
    private final String especialidade;
    private final String descricao;

    Lesma(int numero, String nome, String especialidade, String descricao) {
        this.numero = numero;
        this.nome = nome;
        this.especialidade = especialidade;
        this.descricao = descricao;
    }

    /** Quanto o clima ajuda esta lesma. Todas ficam numa faixa parecida, de 0 a uns 20 pontos. */
    public double bonus(Clima clima, Sorteio sorteio) {
        return switch (this) {
            case GOSMINHA -> Math.max(0, clima.umidade() - 40) / 3.0;           // 70% de umidade = +10
            case LESMALDA -> Math.max(0, 28 - clima.temperatura());             // 18 °C = +10
            case DONA_BABA -> Math.min(clima.vento(), 30) * 0.7;                // 15 km/h = +10,5
            case TURBO_MOLENGA -> sorteio.proximo() * 18;                       // de 0 a 18
            case BRASINHA -> Math.max(0, clima.temperatura() - 18);             // 28 °C = +10
            case MESTRE_ZEN -> Math.max(0, 20 - clima.vento()) * 0.6;           // sem vento = +12
            case GAROA -> clima.chovendo() ? 10 + Math.min(clima.chuva(), 5) * 2 : 0; // chovendo = +10 a +20
            case BARAO -> Math.max(0, 75 - clima.umidade()) / 2.5;              // 50% de umidade = +10
        };
    }

    public static Lesma porCodigo(String codigo) {
        for (Lesma lesma : values()) {
            if (lesma.name().equalsIgnoreCase(codigo)) {
                return lesma;
            }
        }
        throw new RegraDeNegocioException("Lesma inválida: " + codigo);
    }

    public int getNumero() {
        return numero;
    }

    public String getNome() {
        return nome;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public String getDescricao() {
        return descricao;
    }
}
