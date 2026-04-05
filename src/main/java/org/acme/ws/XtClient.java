package org.acme.ws;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.CategoriaExchange;
import org.acme.entity.ParametrizacaoConsultaPreco;

import java.util.Locale;

@ApplicationScoped
public class XtClient extends AbstractExchangeOrderBookClient {

    @Override
    protected String getNomeExchange() {
        return "XT.com";
    }

    @Override
    protected String construirUrlConsulta(ParametrizacaoConsultaPreco parametrizacao, String symbol, int depthLimit) {
        if (obterCategoriaExchange(parametrizacao) == CategoriaExchange.FUTURO) {
            throw new IllegalArgumentException("A XT.com nao possui client FUTURO configurado neste projeto.");
        }

        return obterBaseUrl(parametrizacao) + "/v4/public/depth?symbol=" + symbol + "&limit=" + depthLimit;
    }

    @Override
    protected JsonNode extrairLivroOfertas(JsonNode root) {
        if (root == null || root.isMissingNode()) {
            throw new IllegalStateException("Resposta vazia da XT.com.");
        }

        JsonNode rc = root.get("rc");
        if (rc == null || rc.asInt(-1) != 0) {
            String mc = root.hasNonNull("mc") ? root.get("mc").asText() : "mc ausente";
            throw new IllegalStateException("Falha ao consultar a XT.com: " + mc);
        }

        JsonNode result = root.get("result");
        if (result == null || result.isMissingNode()) {
            throw new IllegalStateException("Resposta da XT.com sem objeto result.");
        }

        return result;
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
        return simbolo.trim().toUpperCase(Locale.ROOT);
    }
}
