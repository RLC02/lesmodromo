package com.lesmodromo.core.domain;

import com.lesmodromo.core.exception.RegraDeNegocioException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Entidade principal: uma aposta numa corrida de lesmas.
 */
public class Corrida {

    public static final int PONTOS_VITORIA = 10;
    public static final int PONTOS_PARTICIPACAO = 1;
    private static final int TAMANHO_MAXIMO_NOME = 20;

    private final String id;
    private final String idCliente;
    private final String jogador;
    private final Lesma lesmaEscolhida;
    private final Cidade cidade;
    private final boolean modoCaos;
    private final Instant criadaEm;
    private StatusCorrida status;
    private Clima clima;
    private boolean climaFallback;
    private int tentativasClima;
    private List<Colocacao> resultado;
    private boolean rankingAtualizado;
    private boolean narrada;

    /** Construtor completo, usado para reconstruir a corrida a partir do banco. */
    public Corrida(String id, String idCliente, String jogador, Lesma lesmaEscolhida, Cidade cidade, boolean modoCaos,
                   Instant criadaEm, StatusCorrida status, Clima clima, boolean climaFallback, int tentativasClima,
                   List<Colocacao> resultado, boolean rankingAtualizado, boolean narrada) {
        this.id = id;
        this.idCliente = idCliente;
        this.jogador = jogador;
        this.lesmaEscolhida = lesmaEscolhida;
        this.cidade = cidade;
        this.modoCaos = modoCaos;
        this.criadaEm = criadaEm;
        this.status = status;
        this.clima = clima;
        this.climaFallback = climaFallback;
        this.tentativasClima = tentativasClima;
        this.resultado = resultado == null ? List.of() : List.copyOf(resultado);
        this.rankingAtualizado = rankingAtualizado;
        this.narrada = narrada;
    }

    /** Cria uma aposta nova, ainda na largada. */
    public static Corrida nova(String idCliente, String jogador, Lesma lesma, Cidade cidade, boolean modoCaos) {
        String nome = jogador == null ? "" : jogador.trim();
        if (nome.isEmpty()) {
            throw new RegraDeNegocioException("Diga seu nome para apostar");
        }
        if (nome.length() > TAMANHO_MAXIMO_NOME) {
            throw new RegraDeNegocioException("O nome pode ter no máximo " + TAMANHO_MAXIMO_NOME + " letras");
        }
        return new Corrida(UUID.randomUUID().toString(), idCliente, nome, lesma, cidade, modoCaos, Instant.now(),
                StatusCorrida.NA_LARGADA, null, false, 0, List.of(), false, false);
    }

    /** O juiz recebeu a mensagem e a corrida começou. */
    public void iniciar() {
        if (status == StatusCorrida.FINALIZADA) {
            throw new RegraDeNegocioException("Essa corrida já terminou");
        }
        this.status = StatusCorrida.CORRENDO;
    }

    public void finalizar(Clima clima, int tentativasClima, boolean climaFallback, List<Colocacao> resultado) {
        if (status != StatusCorrida.CORRENDO) {
            throw new RegraDeNegocioException("A corrida precisa estar correndo para terminar");
        }
        this.clima = clima;
        this.tentativasClima = tentativasClima;
        this.climaFallback = climaFallback;
        this.resultado = List.copyOf(resultado);
        this.status = StatusCorrida.FINALIZADA;
    }

    public boolean finalizada() {
        return status == StatusCorrida.FINALIZADA;
    }

    public Lesma vencedora() {
        return resultado.isEmpty() ? null : resultado.get(0).lesma();
    }

    public boolean venceu() {
        return finalizada() && lesmaEscolhida == vencedora();
    }

    public int pontosGanhos() {
        return venceu() ? PONTOS_VITORIA : PONTOS_PARTICIPACAO;
    }

    public int posicaoDaLesmaEscolhida() {
        return resultado.stream()
                .filter(colocacao -> colocacao.lesma() == lesmaEscolhida)
                .map(Colocacao::posicao)
                .findFirst()
                .orElse(0);
    }

    public String getId() {
        return id;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public String getJogador() {
        return jogador;
    }

    public Lesma getLesmaEscolhida() {
        return lesmaEscolhida;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public boolean isModoCaos() {
        return modoCaos;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public StatusCorrida getStatus() {
        return status;
    }

    public Clima getClima() {
        return clima;
    }

    public boolean isClimaFallback() {
        return climaFallback;
    }

    public int getTentativasClima() {
        return tentativasClima;
    }

    public List<Colocacao> getResultado() {
        return resultado;
    }

    public boolean isRankingAtualizado() {
        return rankingAtualizado;
    }

    public boolean isNarrada() {
        return narrada;
    }
}
