package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Path("/api/teste")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class TesteGraficoResource {

    private static final Logger LOG = Logger.getLogger(TesteGraficoResource.class);

    @GET
    @Path("/grafico-dados")
    public Map<String, Object> getDadosGrafico() {
        LOG.info("Retornando dados de teste para gráfico");
        
        List<Map<String, Object>> dados = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();
        
        // Gera 24 pontos de dados (uma hora cada)
        for (int i = 23; i >= 0; i--) {
            LocalDateTime dataHora = agora.minusHours(i);
            Map<String, Object> ponto = new HashMap<>();
            ponto.put("dataHora", dataHora.toString());
            ponto.put("precoCompra", BigDecimal.valueOf(42000 + Math.random() * 1000));
            ponto.put("precoVenda", BigDecimal.valueOf(42100 + Math.random() * 1000));
            dados.add(ponto);
        }
        
        Map<String, Object> resultado = new HashMap<>();
        Map<String, Object> parametrizacao = new HashMap<>();
        parametrizacao.put("id", 1);
        parametrizacao.put("nome", "BTC/USDT (Teste)");
        parametrizacao.put("dados", dados);
        resultado.put("1", parametrizacao);
        
        return resultado;
    }
}
