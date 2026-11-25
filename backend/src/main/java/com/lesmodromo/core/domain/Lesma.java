package com.lesmodromo.core.domain;

import com.lesmodromo.core.exception.RegraDeNegocioException;

/**
 * As quatro competidoras. Cada uma tem um ponto forte ligado ao clima.
 */
public enum Lesma {

    GOSMINHA(1, "Gosminha", "Umidade", "Quanto mais úmido o ar, mais rápido ela desliza."),
    LESMALDA(2, "Lesmalda", "Frio", "Fica elétrica no frio. No calor, só quer dormir."),
    DONA_BABA(3, "Dona Baba", "Vento", "Abre a concha feito vela e pega carona no vento."),
    TURBO_MOLENGA(4, "Turbo Molenga", "Sorte", "Ignora o clima. Às vezes voa, às vezes cochila.");

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

    /** Quanto o clima ajuda esta lesma. */
    public double bonus(Clima clima, Sorteio sorteio) {
        return switch (this) {
            case GOSMINHA -> Math.max(0, clima.umidade() - 40) / 3.0; // 70% de umidade = +10
            case LESMALDA -> Math.max(0, 28 - clima.temperatura());    // 18 °C = +10
            case DONA_BABA -> Math.min(clima.vento(), 30) * 0.7;       // 15 km/h = +10,5
            case TURBO_MOLENGA -> sorteio.proximo() * 18;              // de 0 a 18
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
