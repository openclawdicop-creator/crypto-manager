package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.SQLExecutionResult;
import org.acme.repository.SQLRepository;

@ApplicationScoped
public class SQLService {

    @Inject
    SQLRepository sqlRepository;

    @Transactional
    public SQLExecutionResult execute(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe um SQL para executar.");
        }

        return sqlRepository.execute(sql.trim());
    }
}
