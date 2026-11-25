package com.lesmodromo.core.domain;

import java.util.List;

/**
 * O clima de agora numa cidade e a vantagem que ele dá a cada lesma.
 */
public record PrevisaoDoPareo(Cidade cidade, LeituraDoClima leitura, List<Vantagem> vantagens) {
}
