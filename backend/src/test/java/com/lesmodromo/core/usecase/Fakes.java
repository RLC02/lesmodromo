package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Cidade;
import com.lesmodromo.core.domain.Clima;
import com.lesmodromo.core.domain.ClimaMedido;
import com.lesmodromo.core.domain.Corrida;
import com.lesmodromo.core.domain.Jogador;
import com.lesmodromo.core.domain.Narracao;
import com.lesmodromo.core.domain.VitoriasDaLesma;
import com.lesmodromo.core.exception.ClimaIndisponivelException;
import com.lesmodromo.core.gateway.ClimaGateway;
import com.lesmodromo.core.gateway.CorridaGateway;
import com.lesmodromo.core.gateway.EventoGateway;
import com.lesmodromo.core.gateway.NarracaoGateway;
import com.lesmodromo.core.gateway.RankingGateway;
import com.lesmodromo.core.gateway.Transacao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Implementações em memória dos gateways. Como o core não depende de Spring nem de banco,
 * os casos de uso são testados só com isso, sem subir nada.
 */
final class Fakes {

    private Fakes() {
    }

    static class Corridas implements CorridaGateway {
        final Map<String, Corrida> porId = new HashMap<>();
        final Set<String> rankingMarcado = new HashSet<>();
        final Set<String> narradas = new HashSet<>();

        @Override
        public Corrida salvar(Corrida corrida) {
            porId.put(corrida.getId(), corrida);
            return corrida;
        }

        @Override
        public Optional<Corrida> buscarPorId(String id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public Optional<Corrida> buscarPorIdCliente(String idCliente) {
            return porId.values().stream().filter(c -> idCliente.equals(c.getIdCliente())).findFirst();
        }

        @Override
        public boolean marcarRankingAtualizado(String id) {
            return rankingMarcado.add(id);
        }

        @Override
        public void marcarNarrada(String id) {
            narradas.add(id);
        }
    }

    static class Eventos implements EventoGateway {
        final List<String> publicados = new ArrayList<>();
        boolean foraDoAr;

        @Override
        public void publicarCorridaLargada(Corrida corrida) {
            publicar("corrida.largada");
        }

        @Override
        public void publicarCorridaFinalizada(Corrida corrida) {
            publicar("corrida.finalizada");
        }

        private void publicar(String evento) {
            if (foraDoAr) {
                throw new IllegalStateException("RabbitMQ fora do ar");
            }
            publicados.add(evento);
        }
    }

    static class ClimaFixo implements ClimaGateway {
        Clima clima = new Clima(20, 90, 5, 0);
        int tentativas = 1;
        boolean foraDoAr;

        @Override
        public ClimaMedido medir(Cidade cidade, boolean modoCaos) {
            if (foraDoAr) {
                throw new ClimaIndisponivelException(4, new IllegalStateException("timeout"));
            }
            return new ClimaMedido(clima, tentativas);
        }
    }

    static class RankingEmMemoria implements RankingGateway {
        final Map<String, Jogador> jogadores = new HashMap<>();

        @Override
        public void registrar(String jogador, int pontos, boolean venceu) {
            Jogador atual = jogadores.getOrDefault(jogador, new Jogador(jogador, 0, 0, 0));
            jogadores.put(jogador, new Jogador(jogador, atual.pontos() + pontos, atual.corridas() + 1,
                    atual.vitorias() + (venceu ? 1 : 0)));
        }

        @Override
        public List<Jogador> melhores(int limite) {
            return new ArrayList<>(jogadores.values());
        }

        @Override
        public List<VitoriasDaLesma> vitoriasPorLesma() {
            return List.of();
        }
    }

    static class Narracoes implements NarracaoGateway {
        final List<Narracao> salvas = new ArrayList<>();

        @Override
        public void salvar(Narracao narracao) {
            salvas.add(narracao);
        }

        @Override
        public List<Narracao> ultimas(int limite) {
            return salvas;
        }
    }

    static class TransacaoDireta implements Transacao {
        int vezes;

        @Override
        public void executar(Runnable acao) {
            vezes++;
            acao.run();
        }
    }
}
