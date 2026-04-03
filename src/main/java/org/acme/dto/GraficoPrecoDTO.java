package org.acme.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GraficoPrecoDTO {
    public LocalDateTime dataHora;
    public BigDecimal precoCompra;
    public BigDecimal precoVenda;

    public GraficoPrecoDTO() {}

    public GraficoPrecoDTO(LocalDateTime dataHora, BigDecimal precoCompra, BigDecimal precoVenda) {
        this.dataHora = dataHora;
        this.precoCompra = precoCompra;
        this.precoVenda = precoVenda;
    }
}
