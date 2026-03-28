package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "parametrizacao_consulta_preco")
public class ParametrizacaoConsultaPreco extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "exchange_id", nullable = false)
    public Exchange exchange;

    @ManyToOne
    @JoinColumn(name = "rede_id", nullable = false)
    public Rede rede;

    @ManyToOne
    @JoinColumn(name = "token_compra_id", nullable = false)
    public AtivoFinanceiro tokenCompra;

    @ManyToOne
    @JoinColumn(name = "token_venda_id", nullable = false)
    public AtivoFinanceiro tokenVenda;

    @Column(name = "quantidade_compra", nullable = false)
    public Double quantidadeCompra;

    @Column(nullable = false)
    public boolean ativa;

    @Column(name = "log_habilitado")
    public boolean logHabilitado;
}
