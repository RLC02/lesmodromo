package com.lesmodromo.core.usecase;

import com.lesmodromo.core.domain.Cidade;
import com.lesmodromo.core.domain.LeituraDoClima;
import com.lesmodromo.core.domain.Pista;
import com.lesmodromo.core.domain.PrevisaoDoPareo;

/**
 * Mostra, antes da aposta, o clima de agora na cidade e qual lesma ele favorece.
 */
public class PreverPareo {

    private final ConsultarClima consultarClima;

    public PreverPareo(ConsultarClima consultarClima) {
        this.consultarClima = consultarClima;
    }

    public PrevisaoDoPareo executar(String codigoCidade) {
        Cidade cidade = Cidade.porCodigo(codigoCidade);
        LeituraDoClima leitura = consultarClima.executar(cidade, false);
        return new PrevisaoDoPareo(cidade, leitura, Pista.vantagens(leitura.clima()));
    }
}
