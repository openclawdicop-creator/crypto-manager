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
import org.acme.ws.BitgetClient;
import org.acme.ws.GateClient;
import org.acme.ws.JupiterClient;
import org.acme.ws.LBankClient;
import org.acme.ws.MexcClient;
import org.acme.ws.XtClient;
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
    GateClient gateClient;

    @Inject
    BitgetClient bitgetClient;

    @Inject
    LBankClient lbankClient;

    @Inject
    XtClient xtClient;

    @Inject
    MexcClient mexcClient;

    @Inject
    JupiterClient jupiterClient;

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

        if (nomeExchangeEquals(nomeExchange, "bybit")) {
            return bybitClient.consultarPreco(parametrizacaoPersistida);
        }

        if (nomeExchangeEquals(nomeExchange, "binance")) {
            return binanceClient.consultarPreco(parametrizacaoPersistida);
        }

        if (nomeExchangeEquals(nomeExchange, "gate", "gate.io")) {
            return gateClient.consultarPreco(parametrizacaoPersistida);
        }

        if (nomeExchangeEquals(nomeExchange, "bitget")) {
            return bitgetClient.consultarPreco(parametrizacaoPersistida);
        }

        if (nomeExchangeEquals(nomeExchange, "lbank")) {
            return lbankClient.consultarPreco(parametrizacaoPersistida);
        }

        if (nomeExchangeEquals(nomeExchange, "xt", "xt.com")) {
            return xtClient.consultarPreco(parametrizacaoPersistida);
        }

        if (nomeExchangeEquals(nomeExchange, "mexc")) {
            return mexcClient.consultarPreco(parametrizacaoPersistida);
        }

        if (nomeExchangeEquals(nomeExchange, "jupiter")) {
            return jupiterClient.consultarPreco(parametrizacaoPersistida);
        }

        throw new IllegalArgumentException("Exchange nao suportada para consulta de cotacao: " + nomeExchange);
    }

    private boolean nomeExchangeEquals(String nomeExchange, String... aliases) {
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(nomeExchange)) {
                return true;
            }
        }
        return false;
    }
}
