package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Exchange;
import org.acme.entity.TipoExchange;

import java.util.List;

@ApplicationScoped
public class ExchangeRepository implements PanacheRepository<Exchange> {

    public List<Exchange> findByTipo(TipoExchange tipo) {
        return list("tipo", tipo);
    }

    public List<Exchange> findByAtivo(boolean logHabilitado) {
        return list("logHabilitado", logHabilitado);
    }
}
