package org.acme.ws;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.CategoriaExchange;
import org.acme.entity.ParametrizacaoConsultaPreco;

@ApplicationScoped
public class BybitClient extends AbstractExchangeOrderBookClient {

    @Override
    protected String getNomeExchange() {
        return "Bybit";
    }

    @Override
    protected String construirUrlConsulta(ParametrizacaoConsultaPreco parametrizacao, String symbol, int depthLimit) {
        return obterBaseUrl(parametrizacao) + "/v5/market/orderbook?category=" + obterCategoriaBybit(parametrizacao)
                + "&symbol=" + symbol + "&limit=" + depthLimit;
    }

    @Override
    protected JsonNode extrairLivroOfertas(JsonNode root) {
        if (root == null || root.isMissingNode()) {
            throw new IllegalStateException("Resposta vazia da Bybit.");
        }

        JsonNode retCode = root.get("retCode");
        if (retCode == null || retCode.asInt(-1) != 0) {
            String retMsg = root.hasNonNull("retMsg") ? root.get("retMsg").asText() : "retMsg ausente";
            throw new IllegalStateException("Falha ao consultar a Bybit: " + retMsg);
        }

        JsonNode result = root.get("result");
        if (result == null || result.isMissingNode()) {
            throw new IllegalStateException("Resposta da Bybit sem objeto result.");
        }

        return result;
    }

    @Override
    protected JsonNode obterOfertasCompra(JsonNode livroOfertas) {
        return livroOfertas.get("a");
    }

    @Override
    protected JsonNode obterOfertasVenda(JsonNode livroOfertas) {
        return livroOfertas.get("b");
    }

    @Override
    protected String getDetalhesConsultaLog(ParametrizacaoConsultaPreco parametrizacao, int depthLimit) {
        CategoriaExchange categoria = obterCategoriaExchange(parametrizacao);
        return "[categoria=" + categoria + ", category=" + obterCategoriaBybit(parametrizacao)
                + ", depthLimit=" + depthLimit + "]";
    }

    private String obterCategoriaBybit(ParametrizacaoConsultaPreco parametrizacao) {
        CategoriaExchange categoria = obterCategoriaExchange(parametrizacao);
        return switch (categoria) {
            case FUTURO -> "linear";
            case SPOT -> "spot";
        };
    }
}
