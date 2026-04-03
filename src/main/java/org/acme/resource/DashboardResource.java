package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.service.DashboardService;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class DashboardResource {

    private static final Logger LOG = Logger.getLogger(DashboardResource.class);

    @Inject
    DashboardService dashboardService;

    /**
     * Retorna dados de variação de preço para todas as parametrizações ativas
     * Usado para popular gráficos do dashboard
     */
    @GET
    @Path("/variacao-preco/todos")
    public ApiResponse<Map<String, Object>> getVariacaoPrecoTodos() {
        try {
            LOG.info("Requisição para dados de variação de preço de todas as parametrizações");
            Map<String, Object> dados = dashboardService.buscarVariacaoPrecoTodasParametrizacoes();
            return ApiResponse.success(dados);
        } catch (Exception e) {
            LOG.error("Erro ao buscar dados do dashboard: " + e.getMessage());
            return ApiResponse.error("Erro ao buscar dados do dashboard: " + e.getMessage());
        }
    }

    /**
     * Retorna dados de variação de preço para uma parametrização específica
     */
    @GET
    @Path("/variacao-preco/{parametrizacaoId}")
    public ApiResponse<List> getVariacaoPrecoPorParametrizacao(@PathParam("parametrizacaoId") Long id) {
        try {
            LOG.info("Requisição para dados de variação de preço da parametrização " + id);
            List dados = dashboardService.buscarVariacaoPreco(id);
            return ApiResponse.success(dados);
        } catch (Exception e) {
            LOG.error("Erro ao buscar dados da parametrização " + id + ": " + e.getMessage());
            return ApiResponse.error("Erro ao buscar dados: " + e.getMessage());
        }
    }
}
