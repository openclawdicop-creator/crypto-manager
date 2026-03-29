package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_cotacao")
public class HistoricoCotacao extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "parametrizacao_id", nullable = false)
    public ParametrizacaoConsultaPreco parametrizacao;

    @Column(name = "data_hora_consulta", nullable = false)
    public LocalDateTime dataHoraConsulta;

    @Column(name = "cotacao_compra", nullable = false)
    public Double cotacaoCompra;

    @Column(name = "cotacao_venda", nullable = false)
    public Double cotacaoVenda;
}
