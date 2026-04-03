package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.AtivoFinanceiroRede;

import java.util.List;

@ApplicationScoped
public class AtivoFinanceiroRedeRepository implements PanacheRepository<AtivoFinanceiroRede> {

    public List<AtivoFinanceiroRede> listAllOrdered() {
        return list("order by ativoFinanceiro.nome asc, rede.nome asc");
    }

    public boolean existsByAtivoAndRede(Long ativoFinanceiroId, Long redeId) {
        return count("ativoFinanceiro.id = ?1 and rede.id = ?2", ativoFinanceiroId, redeId) > 0;
    }

    public boolean existsByAtivoAndRedeExcludingId(Long ativoFinanceiroId, Long redeId, Long id) {
        return count("ativoFinanceiro.id = ?1 and rede.id = ?2 and id <> ?3", ativoFinanceiroId, redeId, id) > 0;
    }
}
