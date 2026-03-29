package org.acme.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class DatabaseIdentitySyncService {

    private static final List<String> IDENTITY_TABLES = List.of(
            "usuario",
            "exchange",
            "rede",
            "ativo_financeiro",
            "parametrizacao_consulta_preco",
            "historico_cotacao"
    );

    @Inject
    EntityManager entityManager;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        for (String table : IDENTITY_TABLES) {
            Number nextId = (Number) entityManager
                    .createNativeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM " + table)
                    .getSingleResult();

            entityManager
                    .createNativeQuery("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH " + nextId.longValue())
                    .executeUpdate();
        }
    }
}
