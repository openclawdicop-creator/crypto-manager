package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "exchange")
public class Exchange extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 100)
    public String nome;

    @Column(length = 500)
    public String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TipoExchange tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_api", nullable = false)
    public TipoApi tipoApi;

    @Column(name = "token_api", length = 500)
    public String tokenApi;

    @Column(name = "url_api", length = 200)
    public String urlApi;

    @Column(name = "log_habilitado")
    public boolean logHabilitado;

    @Column(name = "usar_proxy")
    public Boolean usarProxy = false;
}
