package org.acme.ws;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.CategoriaExchange;
import org.acme.entity.ParametrizacaoConsultaPreco;

import java.util.Locale;

@ApplicationScoped
public class BitgetClient extends AbstractExchangeOrderBookClient {

    @Override
    protected String getNomeExchange() {
        return "Bitget";
    }

    @Override
    protected String construirUrlConsulta(ParametrizacaoConsultaPreco parametrizacao, String symbol, int depthLimit) {
        return obterBaseUrl(parametrizacao)
                + "/api/v3/market/orderbook?category=" + obterCategoriaBitget(parametrizacao)
                + "&symbol=" + symbol
                + "&limit=" + depthLimit;
    }

    @Override
    protected JsonNode extrairLivroOfertas(JsonNode root) {
        if (root == null || root.isMissingNode()) {
            throw new IllegalStateException("Resposta vazia da Bitget.");
        }

        JsonNode code = root.get("code");
        if (code == null || !"00000".equals(code.asText())) {
            String msg = root.hasNonNull("msg") ? root.get("msg").asText() : "msg ausente";
            throw new IllegalStateException("Falha ao consultar a Bitget: " + msg);
        }

        JsonNode data = root.get("data");
        if (data == null || data.isMissingNode()) {
            throw new IllegalStateException("Resposta da Bitget sem objeto data.");
        }

        return data;
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
        return "[categoria=" + categoria + ", category=" + obterCategoriaBitget(parametrizacao)
                + ", depthLimit=" + depthLimit + "]";
    }

    @Override
    protected String normalizarSimbolo(String simbolo) {
        return simbolo.trim().replace("_", "").toUpperCase(Locale.ROOT);
    }

    private String obterCategoriaBitget(ParametrizacaoConsultaPreco parametrizacao) {
        CategoriaExchange categoria = obterCategoriaExchange(parametrizacao);
        return categoria == CategoriaExchange.FUTURO ? "USDT-FUTURES" : "SPOT";
    }
}
