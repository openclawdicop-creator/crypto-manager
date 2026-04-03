package org.acme.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ativo_financeiro")
public class AtivoFinanceiro extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 100)
    public String nome;

    @Column(unique = true, nullable = false, length = 10)
    public String simbolo;

    @OneToMany(mappedBy = "ativoFinanceiro", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("ativoFinanceiro")
    public List<AtivoFinanceiroRede> redes = new ArrayList<>();

}
