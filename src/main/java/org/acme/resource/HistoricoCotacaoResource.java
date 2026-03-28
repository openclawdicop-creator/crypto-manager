package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.entity.HistoricoCotacao;
import org.acme.service.HistoricoCotacaoService;

import java.util.List;

@Path("/api/historicos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HistoricoCotacaoResource {

    @Inject
    HistoricoCotacaoService historicoCotacaoService;

    @GET
    public ApiResponse<List<HistoricoCotacao>> listarTodos() {
        try {
            List<HistoricoCotacao> historicos = historicoCotacaoService.listarTodos();
            return ApiResponse.success(historicos);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar históricos de cotação: " + e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public ApiResponse<HistoricoCotacao> buscarPorId(@PathParam("id") Long id) {
        try {
            return historicoCotacaoService.buscarPorId(id)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error("Histórico de cotação não encontrado"));
        } catch (Exception e) {
            return ApiResponse.error("Erro ao buscar histórico de cotação: " + e.getMessage());
        }
    }

    @GET
    @Path("/parametrizacao/{parametrizacaoId}")
    public ApiResponse<List<HistoricoCotacao>> listarPorParametrizacao(@PathParam("parametrizacaoId") Long parametrizacaoId) {
        try {
            List<HistoricoCotacao> historicos = historicoCotacaoService.listarPorParametrizacaoId(parametrizacaoId);
            return ApiResponse.success(historicos);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar históricos de cotação: " + e.getMessage());
        }
    }

    @POST
    public ApiResponse<HistoricoCotacao> criar(HistoricoCotacao historicoCotacao) {
        try {
            HistoricoCotacao criado = historicoCotacaoService.criar(historicoCotacao);
            return ApiResponse.success("Histórico de cotação criado com sucesso", criado);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao criar histórico de cotação: " + e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public ApiResponse<Void> excluir(@PathParam("id") Long id) {
        try {
            historicoCotacaoService.excluir(id);
            return ApiResponse.success("Histórico de cotação excluído com sucesso", null);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao excluir histórico de cotação: " + e.getMessage());
        }
    }
}
