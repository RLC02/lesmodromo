package com.lesmodromo.framework.persistence;

import com.lesmodromo.framework.persistence.document.CorridaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CorridaRepository extends MongoRepository<CorridaDocument, String> {

    Optional<CorridaDocument> findByIdCliente(String idCliente);
}
