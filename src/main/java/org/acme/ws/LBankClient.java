package org.acme.ws;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.CategoriaExchange;
import org.acme.entity.ParametrizacaoConsultaPreco;

import java.util.Locale;

@ApplicationScoped
public class LBankClient extends AbstractExchangeOrderBookClient {

    @Override
    protected String getNomeExchange() {
        return "LBank";
    }

    @Override
    protected String construirUrlConsulta(ParametrizacaoConsultaPreco parametrizacao, String symbol, int depthLimit) {
        if (obterCategoriaExchange(parametrizacao) == CategoriaExchange.FUTURO) {
            throw new IllegalArgumentException("A LBank nao possui client FUTURO configurado neste projeto.");
        }

        return obterBaseUrl(parametrizacao) + "/v2/depth.do?symbol=" + symbol + "&size=" + depthLimit;
    }

    @Override
    protected JsonNode extrairLivroOfertas(JsonNode root) {
        if (root == null || root.isMissingNode()) {
            throw new IllegalStateException("Resposta vazia da LBank.");
        }

        JsonNode asks = root.get("asks");
        JsonNode bids = root.get("bids");
        if (asks != null && bids != null) {
            return root;
        }

        JsonNode data = root.get("data");
        if (data != null && !data.isMissingNode() && data.has("asks") && data.has("bids")) {
            return data;
        }

        String mensagemErro = root.hasNonNull("msg") ? root.get("msg").asText()
                : root.hasNonNull("message") ? root.get("message").asText()
                : "resposta sem asks/bids";
        throw new IllegalStateException("Falha ao consultar a LBank: " + mensagemErro);
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
    protected String normalizarSimbolo(String simbolo) {
        return simbolo.trim().toLowerCase(Locale.ROOT);
    }
}
