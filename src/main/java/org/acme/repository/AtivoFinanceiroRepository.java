package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.AtivoFinanceiro;

@ApplicationScoped
public class AtivoFinanceiroRepository implements PanacheRepository<AtivoFinanceiro> {

    public AtivoFinanceiro findBySimbolo(String simbolo) {
        return find("simbolo", simbolo).firstResult();
    }

    public boolean existsBySimbolo(String simbolo) {
        return count("simbolo", simbolo) > 0;
    }
}
