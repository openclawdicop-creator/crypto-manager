package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "ativo_financeiro_rede",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ativo_financeiro_rede_ativo_rede", columnNames = {"ativo_financeiro_id", "rede_id"})
        }
)
public class AtivoFinanceiroRede extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ativo_financeiro_id", nullable = false)
    public AtivoFinanceiro ativoFinanceiro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rede_id", nullable = false)
    public Rede rede;

    @Column(nullable = false, length = 150)
    public String identificador;

    @Column(name = "quantidade_casas_decimais", nullable = false)
    public Integer quantidadeCasasDecimais = 6;
}
