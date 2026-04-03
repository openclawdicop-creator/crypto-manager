package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracao_sistema")
public class ConfiguracaoSistema extends PanacheEntityBase {

    @Id
    public String chave;

    @Column(nullable = false)
    public String valor;

    public ConfiguracaoSistema() {}

    public ConfiguracaoSistema(String chave, String valor) {
        this.chave = chave;
        this.valor = valor;
    }
}
