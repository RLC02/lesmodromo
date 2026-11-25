package com.lesmodromo.framework.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia do RabbitMQ (publish/subscribe):
 *
 *                                      ┌─ corrida.largada ────► fila lesmodromo.juiz      (JuizListener)
 *   EventoGatewayRabbit ─► exchange ───┤
 *                 lesmodromo.eventos   └─ corrida.finalizada ─┬► fila lesmodromo.ranking  (RankingListener)
 *                       (topic)                               └► fila lesmodromo.narrador (NarradorListener)
 *
 * Um mesmo evento "corrida.finalizada" chega em DUAS filas, e cada consumidor faz a sua parte sem conhecer o outro.
 * Mensagem que falha 3 vezes vai para a dead letter: exchange lesmodromo.eventos.falhas -> fila lesmodromo.falhas.
 * As filas e exchanges são criadas sozinhas no RabbitMQ quando a aplicação sobe.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "lesmodromo.eventos";
    public static final String EXCHANGE_FALHAS = "lesmodromo.eventos.falhas";

    public static final String CORRIDA_LARGADA = "corrida.largada";
    public static final String CORRIDA_FINALIZADA = "corrida.finalizada";

    public static final String FILA_JUIZ = "lesmodromo.juiz";
    public static final String FILA_RANKING = "lesmodromo.ranking";
    public static final String FILA_NARRADOR = "lesmodromo.narrador";
    public static final String FILA_FALHAS = "lesmodromo.falhas";

    @Bean
    public TopicExchange eventos() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public FanoutExchange eventosFalhas() {
        return new FanoutExchange(EXCHANGE_FALHAS);
    }

    @Bean
    public Queue filaJuiz() {
        return filaComDeadLetter(FILA_JUIZ);
    }

    @Bean
    public Queue filaRanking() {
        return filaComDeadLetter(FILA_RANKING);
    }

    @Bean
    public Queue filaNarrador() {
        return filaComDeadLetter(FILA_NARRADOR);
    }

    @Bean
    public Queue filaFalhas() {
        return QueueBuilder.durable(FILA_FALHAS).build();
    }

    @Bean
    public Binding juizOuveCorridaLargada() {
        return BindingBuilder.bind(filaJuiz()).to(eventos()).with(CORRIDA_LARGADA);
    }

    @Bean
    public Binding rankingOuveCorridaFinalizada() {
        return BindingBuilder.bind(filaRanking()).to(eventos()).with(CORRIDA_FINALIZADA);
    }

    @Bean
    public Binding narradorOuveCorridaFinalizada() {
        return BindingBuilder.bind(filaNarrador()).to(eventos()).with(CORRIDA_FINALIZADA);
    }

    @Bean
    public Binding falhasOuveDeadLetter() {
        return BindingBuilder.bind(filaFalhas()).to(eventosFalhas());
    }

    /** As mensagens trafegam em JSON. */
    @Bean
    public MessageConverter conversorJson(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter conversor = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper tipos = new DefaultJackson2JavaTypeMapper();
        tipos.setTrustedPackages("com.lesmodromo.framework.messaging");
        conversor.setJavaTypeMapper(tipos);
        return conversor;
    }

    private Queue filaComDeadLetter(String nome) {
        return QueueBuilder.durable(nome)
                .deadLetterExchange(EXCHANGE_FALHAS)
                .build();
    }
}
