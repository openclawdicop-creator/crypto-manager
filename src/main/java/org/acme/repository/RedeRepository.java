package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Rede;

@ApplicationScoped
public class RedeRepository implements PanacheRepository<Rede> {
}
