package org.acme.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.entity.ParametrizacaoConsultaPreco;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

@ApplicationScoped
public class BinanceClient {

    private static final int DEPTH_LIMIT = 10;
    private static final String DEPTH_URL = "https://api.binance.com/api/v3/depth";
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    @Inject
    @RestClient
    BinanceApi binanceApi;

    @Inject
    ObjectMapper objectMapper;

    public ResultadoCotacao consultarPreco(ParametrizacaoConsultaPreco parametrizacao) {
        validarParametrizacao(parametrizacao);

        String symbol = parametrizacao.identificadorNegociacao.trim().toUpperCase(Locale.ROOT);
        if (parametrizacao.logHabilitado) {
            System.out.println("URL Invocada: " + DEPTH_URL + "?symbol=" + symbol + "&limit=10");
        }

        String responseBody;
        try {
            responseBody = binanceApi.consultarProfundidade(symbol, DEPTH_LIMIT);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao consultar a Binance: " + e.getMessage(), e);
        }

        if (parametrizacao.logHabilitado) {
            System.out.println("Retorno API: " + responseBody);
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            BigDecimal valorFinanceiroTotal = BigDecimal.valueOf(parametrizacao.quantidadePagamento);

            double cotacaoCompra = calcularPrecoMedioPonderado(root.get("asks"), valorFinanceiroTotal, "compra");
            double cotacaoVenda = calcularPrecoMedioPonderado(root.get("bids"), valorFinanceiroTotal, "venda");

            return new ResultadoCotacao(cotacaoCompra, cotacaoVenda);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao processar o retorno da Binance.", e);
        }
    }

    private void validarParametrizacao(ParametrizacaoConsultaPreco parametrizacao) {
        if (parametrizacao == null) {
            throw new IllegalArgumentException("Parametrizacao obrigatoria para consultar preco.");
        }
        if (parametrizacao.identificadorNegociacao == null || parametrizacao.identificadorNegociacao.isBlank()) {
            throw new IllegalArgumentException("Identificador de negociacao obrigatorio para consultar a Binance.");
        }
        if (parametrizacao.quantidadePagamento == null || parametrizacao.quantidadePagamento <= 0) {
            throw new IllegalArgumentException("Quantidade de pagamento deve ser maior que zero.");
        }
    }

    private double calcularPrecoMedioPonderado(JsonNode ofertas, BigDecimal valorFinanceiroDesejado, String lado) {
        if (ofertas == null || !ofertas.isArray() || ofertas.isEmpty()) {
            throw new IllegalStateException("Livro de ofertas vazio para operacao de " + lado + ".");
        }

        BigDecimal valorFinanceiroRestante = valorFinanceiroDesejado;
        BigDecimal valorFinanceiroExecutado = BigDecimal.ZERO;
        BigDecimal quantidadeBaseExecutada = BigDecimal.ZERO;

        for (JsonNode oferta : ofertas) {
            if (!oferta.isArray() || oferta.size() < 2) {
                continue;
            }

            BigDecimal preco = new BigDecimal(oferta.get(0).asText());
            BigDecimal quantidadeDisponivel = new BigDecimal(oferta.get(1).asText());
            BigDecimal valorFinanceiroDisponivel = preco.multiply(quantidadeDisponivel, MATH_CONTEXT);

            if (valorFinanceiroRestante.compareTo(valorFinanceiroDisponivel) >= 0) {
                valorFinanceiroExecutado = valorFinanceiroExecutado.add(valorFinanceiroDisponivel, MATH_CONTEXT);
                quantidadeBaseExecutada = quantidadeBaseExecutada.add(quantidadeDisponivel, MATH_CONTEXT);
                valorFinanceiroRestante = valorFinanceiroRestante.subtract(valorFinanceiroDisponivel, MATH_CONTEXT);
            } else {
                BigDecimal quantidadeParcial = valorFinanceiroRestante.divide(preco, MATH_CONTEXT);
                valorFinanceiroExecutado = valorFinanceiroExecutado.add(valorFinanceiroRestante, MATH_CONTEXT);
                quantidadeBaseExecutada = quantidadeBaseExecutada.add(quantidadeParcial, MATH_CONTEXT);
                valorFinanceiroRestante = BigDecimal.ZERO;
            }

            if (valorFinanceiroRestante.signum() <= 0) {
                break;
            }
        }

        if (quantidadeBaseExecutada.signum() == 0 || valorFinanceiroRestante.signum() > 0) {
            throw new IllegalStateException("Profundidade insuficiente para calcular a cotacao de " + lado + " com o valor financeiro informado.");
        }

        return valorFinanceiroExecutado.divide(quantidadeBaseExecutada, MATH_CONTEXT).doubleValue();
    }

    public record ResultadoCotacao(double cotacaoCompra, double cotacaoVenda) {
    }
}
