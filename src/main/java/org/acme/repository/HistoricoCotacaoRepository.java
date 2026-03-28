package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.HistoricoCotacao;

import java.util.List;

@ApplicationScoped
public class HistoricoCotacaoRepository implements PanacheRepository<HistoricoCotacao> {

    public List<HistoricoCotacao> findByParametrizacaoId(Long parametrizacaoId) {
        return list("parametrizacao.id", parametrizacaoId);
    }
}
