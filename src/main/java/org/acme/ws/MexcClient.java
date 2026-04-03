package org.acme.ws;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.ParametrizacaoConsultaPreco;

@ApplicationScoped
public class MexcClient extends AbstractExchangeOrderBookClient {

    @Override
    protected String getNomeExchange() {
        return "MEXC";
    }

    @Override
    protected String construirUrlConsulta(ParametrizacaoConsultaPreco parametrizacao, String symbol, int depthLimit) {
        return obterBaseUrl(parametrizacao) + "/api/v3/depth?symbol=" + symbol + "&limit=" + depthLimit;
    }

    @Override
    protected JsonNode extrairLivroOfertas(JsonNode root) {
        if (root == null || root.isMissingNode()) {
            throw new IllegalStateException("Resposta vazia da MEXC.");
        }

        JsonNode bids = root.get("bids");
        JsonNode asks = root.get("asks");
        if (bids != null && asks != null) {
            return root;
        }

        String mensagemErro = root.hasNonNull("msg") ? root.get("msg").asText()
                : root.hasNonNull("message") ? root.get("message").asText()
                : "resposta sem bids/asks";
        String codigoErro = root.hasNonNull("code") ? root.get("code").asText() : null;

        throw new IllegalStateException("Falha ao consultar a MEXC"
                + (codigoErro != null ? " [code=" + codigoErro + "]" : "")
                + ": " + mensagemErro);
    }

    @Override
    protected JsonNode obterOfertasCompra(JsonNode livroOfertas) {
        return livroOfertas.get("asks");
    }

    @Override
    protected JsonNode obterOfertasVenda(JsonNode livroOfertas) {
        return livroOfertas.get("bids");
    }
}
