package com.lesmodromo.framework.persistence;

import com.lesmodromo.core.gateway.Transacao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Implementa a porta Transacao do core usando transação do MongoDB (o Atlas roda em replica set, que suporta).
 * Tudo que roda dentro de executar() é confirmado junto, ou desfeito junto se der erro.
 */
@Component
public class TransacaoMongo implements Transacao {

    private final TransactionTemplate transactionTemplate;

    public TransacaoMongo(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void executar(Runnable acao) {
        transactionTemplate.executeWithoutResult(status -> acao.run());
    }
}
