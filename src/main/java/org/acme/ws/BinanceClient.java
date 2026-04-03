package org.acme.ws;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.ParametrizacaoConsultaPreco;

@ApplicationScoped
public class BinanceClient extends AbstractExchangeOrderBookClient {

    @Override
    protected String getNomeExchange() {
        return "Binance";
    }

    @Override
    protected String construirUrlConsulta(ParametrizacaoConsultaPreco parametrizacao, String symbol, int depthLimit) {
        return obterBaseUrl(parametrizacao) + "/api/v3/depth?symbol=" + symbol + "&limit=" + depthLimit;
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
