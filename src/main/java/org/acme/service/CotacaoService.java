package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.HistoricoCotacao;
import org.acme.entity.ParametrizacaoConsultaPreco;
import org.acme.repository.HistoricoCotacaoRepository;
import org.acme.repository.ParametrizacaoConsultaPrecoRepository;
import org.acme.ws.BinanceClient;
import org.acme.ws.BybitClient;
import org.acme.ws.MexcClient;
import org.acme.ws.ResultadoCotacao;

import java.time.LocalDateTime;

@ApplicationScoped
public class CotacaoService {

    @Inject
    ParametrizacaoConsultaPrecoRepository parametrizacaoConsultaPrecoRepository;

    @Inject
    HistoricoCotacaoRepository historicoCotacaoRepository;

    @Inject
    BinanceClient binanceClient;

    @Inject
    BybitClient bybitClient;

    @Inject
    MexcClient mexcClient;

    @Transactional
    public HistoricoCotacao processarConsulta(ParametrizacaoConsultaPreco parametrizacaoRecebida) {
        if (parametrizacaoRecebida == null || parametrizacaoRecebida.id == null) {
            throw new IllegalArgumentException("A parametrizacao deve estar salva antes da consulta.");
        }

        ParametrizacaoConsultaPreco parametrizacaoPersistida =
                parametrizacaoConsultaPrecoRepository.findById(parametrizacaoRecebida.id);

        if (parametrizacaoPersistida == null) {
            throw new IllegalArgumentException("Parametrizacao nao encontrada para o ID informado.");
        }

        ResultadoCotacao resultado = consultarPrecoNaExchange(parametrizacaoPersistida);

        HistoricoCotacao historicoCotacao = new HistoricoCotacao();
        historicoCotacao.parametrizacao = parametrizacaoPersistida;
        historicoCotacao.dataHoraConsulta = LocalDateTime.now();
        historicoCotacao.cotacaoCompra = resultado.cotacaoCompra();
        historicoCotacao.cotacaoVenda = resultado.cotacaoVenda();

        historicoCotacaoRepository.persist(historicoCotacao);
        return historicoCotacao;
    }

    private ResultadoCotacao consultarPrecoNaExchange(ParametrizacaoConsultaPreco parametrizacaoPersistida) {
        String nomeExchange = parametrizacaoPersistida.exchange != null && parametrizacaoPersistida.exchange.nome != null
                ? parametrizacaoPersistida.exchange.nome.trim()
                : "";

        if ("bybit".equalsIgnoreCase(nomeExchange)) {
            return bybitClient.consultarPreco(parametrizacaoPersistida);
        }

        if ("binance".equalsIgnoreCase(nomeExchange)) {
            return binanceClient.consultarPreco(parametrizacaoPersistida);
        }

        if ("mexc".equalsIgnoreCase(nomeExchange)) {
            return mexcClient.consultarPreco(parametrizacaoPersistida);
        }

        throw new IllegalArgumentException("Exchange nao suportada para consulta de cotacao: " + nomeExchange);
    }
}
