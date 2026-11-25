package com.lesmodromo.framework.persistence;

import com.lesmodromo.core.domain.Usuario;
import com.lesmodromo.core.gateway.UsuarioGateway;
import com.lesmodromo.framework.persistence.document.UsuarioDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class UsuarioGatewayMongo implements UsuarioGateway {

    private final MongoTemplate mongoTemplate;

    public UsuarioGatewayMongo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        mongoTemplate.insert(new UsuarioDocument(usuario.id(), usuario.nome(), Usuario.chave(usuario.nome()),
                usuario.senhaHash(), Instant.now()));
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorNome(String nome) {
        Query porChave = Query.query(Criteria.where("nomeChave").is(Usuario.chave(nome)));
        return Optional.ofNullable(mongoTemplate.findOne(porChave, UsuarioDocument.class)).map(this::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, UsuarioDocument.class)).map(this::paraDominio);
    }

    private Usuario paraDominio(UsuarioDocument doc) {
        return new Usuario(doc.id(), doc.nome(), doc.senhaHash());
    }
}
