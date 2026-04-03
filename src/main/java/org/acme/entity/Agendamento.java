package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "agendamento")
public class Agendamento extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String nome;

    @Column(name = "frequencia_segundos", nullable = false)
    public Integer frequenciaSegundos;

    @Column(nullable = false)
    public boolean ativo;
}
