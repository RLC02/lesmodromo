package com.lesmodromo.framework.resilience;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryExecutorTest {

    private final RetryExecutor retry = new RetryExecutor();
    private final Duration rapido = Duration.ofMillis(1);

    @Test
    void tentaDeNovoAteDarCerto() {
        AtomicInteger chamadas = new AtomicInteger();

        String resultado = retry.executar("teste", 4, rapido, tentativa -> {
            if (chamadas.incrementAndGet() < 3) {
                throw new IllegalStateException("caiu");
            }
            return "ok";
        });

        assertEquals("ok", resultado);
        assertEquals(3, chamadas.get());
    }

    @Test
    void desisteDepoisDoLimite() {
        AtomicInteger chamadas = new AtomicInteger();

        RetryEsgotadoException erro = assertThrows(RetryEsgotadoException.class,
                () -> retry.executar("teste", 3, rapido, tentativa -> {
                    chamadas.incrementAndGet();
                    throw new IllegalStateException("fora do ar");
                }));

        assertEquals(3, chamadas.get());
        assertEquals(3, erro.getTentativas());
    }

    @Test
    void modoCaosFazAsPrimeirasTentativasFalharem() {
        List<Integer> tentativasQueRodaram = new ArrayList<>();

        retry.executar("teste", 4, rapido, 2, tentativa -> tentativasQueRodaram.add(tentativa));

        assertEquals(List.of(3), tentativasQueRodaram);
    }

    @Test
    void esperaDobraACadaTentativaAteOTeto() {
        Duration inicial = Duration.ofMillis(500);

        assertEquals(500, RetryExecutor.calcularEspera(inicial, 1).toMillis());
        assertEquals(1000, RetryExecutor.calcularEspera(inicial, 2).toMillis());
        assertEquals(2000, RetryExecutor.calcularEspera(inicial, 3).toMillis());
        assertEquals(15000, RetryExecutor.calcularEspera(inicial, 10).toMillis());
    }
}
