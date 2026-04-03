package org.acme.service;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.GraficoPrecoDTO;
import org.acme.entity.HistoricoCotacao;
import org.acme.entity.ParametrizacaoConsultaPreco;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class DashboardService {

    private static final Logger LOG = Logger.getLogger(DashboardService.class);

    /**
     * Busca dados de variação de preço das últimas 24 horas para uma parametrização específica
     */
    public List<GraficoPrecoDTO> buscarVariacaoPreco(Long parametrizacaoId) {
        try {
            LocalDateTime vinteQuatroHorasAtras = LocalDateTime.now().minusHours(24);
            
            // Busca históricos das últimas 24 horas
            List<HistoricoCotacao> historicos = HistoricoCotacao.list(
                "parametrizacao.id = ?1 and dataHoraConsulta >= ?2",
                Sort.ascending("dataHoraConsulta"),
                parametrizacaoId,
                vinteQuatroHorasAtras
            );

            LOG.debug("Encontrados " + historicos.size() + " registros para parametrização " + parametrizacaoId);

            return historicos.stream()
                .map(h -> new GraficoPrecoDTO(
                    h.dataHoraConsulta,
                    BigDecimal.valueOf(h.cotacaoCompra),
                    BigDecimal.valueOf(h.cotacaoVenda)
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Erro ao buscar variação de preço: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Busca dados de variação de preço para todas as parametrizações ativas
     */
    public Map<String, Object> buscarVariacaoPrecoTodasParametrizacoes() {
        try {
            List<ParametrizacaoConsultaPreco> ativas = ParametrizacaoConsultaPreco.list("ativa", true);
            Map<String, Object> resultado = new HashMap<>();

            for (ParametrizacaoConsultaPreco p : ativas) {
                Map<String, Object> dadosParametrizacao = new HashMap<>();
                dadosParametrizacao.put("id", p.id);
                dadosParametrizacao.put("nome", p.identificadorNegociacao);
                dadosParametrizacao.put("dados", buscarVariacaoPreco(p.id));
                resultado.put(String.valueOf(p.id), dadosParametrizacao);
            }

            LOG.info("Dashboard: " + ativas.size() + " parametrizações ativas encontradas");
            return resultado;
        } catch (Exception e) {
            LOG.error("Erro ao buscar dados do dashboard: " + e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}
