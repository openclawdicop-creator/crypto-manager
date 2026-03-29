package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.HistoricoCotacao;
import org.acme.entity.ParametrizacaoConsultaPreco;
import org.acme.repository.HistoricoCotacaoRepository;
import org.acme.repository.ParametrizacaoConsultaPrecoRepository;
import org.acme.ws.BinanceClient;

import java.time.LocalDateTime;

@ApplicationScoped
public class CotacaoService {

    @Inject
    ParametrizacaoConsultaPrecoRepository parametrizacaoConsultaPrecoRepository;

    @Inject
    HistoricoCotacaoRepository historicoCotacaoRepository;

    @Inject
    BinanceClient binanceClient;

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

        BinanceClient.ResultadoCotacao resultado = binanceClient.consultarPreco(parametrizacaoPersistida);

        HistoricoCotacao historicoCotacao = new HistoricoCotacao();
        historicoCotacao.parametrizacao = parametrizacaoPersistida;
        historicoCotacao.dataHoraConsulta = LocalDateTime.now();
        historicoCotacao.cotacaoCompra = resultado.cotacaoCompra();
        historicoCotacao.cotacaoVenda = resultado.cotacaoVenda();

        historicoCotacaoRepository.persist(historicoCotacao);
        return historicoCotacao;
    }
}
