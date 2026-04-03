package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "proxy")
public class Proxy extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 100)
    public String nome;

    @Column(nullable = false, length = 500)
    public String url;

    @Column(length = 100)
    public String usuario;

    @Column(length = 100)
    public String senha;

    @Column(length = 500)
    public String token;

    @Column
    public Integer porta;
}
