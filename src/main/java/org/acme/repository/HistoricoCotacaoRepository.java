package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.HistoricoCotacao;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import java.util.List;

@ApplicationScoped
public class HistoricoCotacaoRepository implements PanacheRepository<HistoricoCotacao> {

    public List<HistoricoCotacao> findByParametrizacaoId(Long parametrizacaoId, int page, int size) {
        return find("parametrizacao.id", Sort.descending("dataHoraConsulta"), parametrizacaoId)
                .page(Page.of(page, size))
                .list();
    }

    public long countByParametrizacaoId(Long parametrizacaoId) {
        return count("parametrizacao.id", parametrizacaoId);
    }
}
