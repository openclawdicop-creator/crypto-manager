package org.acme.ws;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.CategoriaExchange;
import org.acme.entity.ParametrizacaoConsultaPreco;

import java.util.Locale;

@ApplicationScoped
public class GateClient extends AbstractExchangeOrderBookClient {

    @Override
    protected String getNomeExchange() {
        return "Gate.io";
    }

    @Override
    protected String construirUrlConsulta(ParametrizacaoConsultaPreco parametrizacao, String symbol, int depthLimit) {
        CategoriaExchange categoria = obterCategoriaExchange(parametrizacao);

        if (categoria == CategoriaExchange.FUTURO) {
            return obterBaseUrl(parametrizacao)
                    + "/api/v4/futures/usdt/order_book?contract=" + symbol
                    + "&limit=" + depthLimit;
        }

        return obterBaseUrl(parametrizacao)
                + "/api/v4/spot/order_book?currency_pair=" + symbol
                + "&limit=" + depthLimit;
    }

    @Override
    protected JsonNode extrairLivroOfertas(JsonNode root) {
        if (root == null || root.isMissingNode()) {
            throw new IllegalStateException("Resposta vazia da Gate.io.");
        }

        if (root.has("asks") && root.has("bids")) {
            return root;
        }

        throw new IllegalStateException("Resposta da Gate.io sem asks/bids.");
    }

    @Override
    protected JsonNode obterOfertasCompra(JsonNode livroOfertas) {
        return livroOfertas.get("asks");
    }

    @Override
    protected JsonNode obterOfertasVenda(JsonNode livroOfertas) {
        return livroOfertas.get("bids");
    }

    @Override
    protected String getDetalhesConsultaLog(ParametrizacaoConsultaPreco parametrizacao, int depthLimit) {
        CategoriaExchange categoria = obterCategoriaExchange(parametrizacao);
        return "[categoria=" + categoria + ", depthLimit=" + depthLimit + "]";
    }

    @Override
    protected String normalizarSimbolo(String simbolo) {
        return simbolo.trim().toUpperCase(Locale.ROOT);
    }
}
