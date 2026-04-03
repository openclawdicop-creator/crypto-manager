package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Proxy;

@ApplicationScoped
public class ProxyRepository implements PanacheRepository<Proxy> {
    // Custom queries can be added here if needed
}
