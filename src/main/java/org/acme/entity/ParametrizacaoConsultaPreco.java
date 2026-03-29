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
    @JoinColumn(name = "ativo_desejado_id", nullable = false)
    public AtivoFinanceiro ativoDesejado;

    @ManyToOne
    @JoinColumn(name = "ativo_pagamento_id", nullable = false)
    public AtivoFinanceiro ativoPagamento;

    @Column(name = "quantidade_pagamento", nullable = false)
    public Double quantidadePagamento;

    @Column(name = "identificador_negociacao", length = 100)
    public String identificadorNegociacao;

    @Column(nullable = false)
    public boolean ativa;

    @Column(name = "log_habilitado")
    public boolean logHabilitado;
}
