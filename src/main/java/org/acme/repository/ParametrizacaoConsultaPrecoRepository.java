package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.ParametrizacaoConsultaPreco;

import java.util.List;

@ApplicationScoped
public class ParametrizacaoConsultaPrecoRepository implements PanacheRepository<ParametrizacaoConsultaPreco> {

    public List<ParametrizacaoConsultaPreco> findByAtiva(boolean ativa) {
        return list("ativa", ativa);
    }

    public List<ParametrizacaoConsultaPreco> findByExchangeId(Long exchangeId) {
        return list("exchange.id", exchangeId);
    }
}
