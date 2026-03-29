package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.entity.ParametrizacaoConsultaPreco;
import org.acme.service.ParametrizacaoConsultaPrecoService;

import java.util.List;

@Path("/api/parametrizacoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ParametrizacaoConsultaPrecoResource {

    @Inject
    ParametrizacaoConsultaPrecoService parametrizacaoService;

    @GET
    public ApiResponse<List<ParametrizacaoConsultaPreco>> listarTodos() {
        try {
            List<ParametrizacaoConsultaPreco> parametrizacoes = parametrizacaoService.listarTodos();
            return ApiResponse.success(parametrizacoes);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar parametrizações: " + e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public ApiResponse<ParametrizacaoConsultaPreco> buscarPorId(@PathParam("id") Long id) {
        try {
            return parametrizacaoService.buscarPorId(id)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error("Parametrização não encontrada"));
        } catch (Exception e) {
            return ApiResponse.error("Erro ao buscar parametrização: " + e.getMessage());
        }
    }

    @GET
    @Path("/ativas")
    public ApiResponse<List<ParametrizacaoConsultaPreco>> listarAtivas() {
        try {
            List<ParametrizacaoConsultaPreco> parametrizacoes = parametrizacaoService.listarPorAtiva(true);
            return ApiResponse.success(parametrizacoes);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar parametrizações ativas: " + e.getMessage());
        }
    }

    @POST
    public ApiResponse<ParametrizacaoConsultaPreco> criar(ParametrizacaoConsultaPreco parametrizacao) {
        try {
            ParametrizacaoConsultaPreco criada = parametrizacaoService.criar(parametrizacao);
            return ApiResponse.success("Parametrização criada com sucesso", criada);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao criar parametrização: " + e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    public ApiResponse<ParametrizacaoConsultaPreco> atualizar(@PathParam("id") Long id, ParametrizacaoConsultaPreco parametrizacao) {
        try {
            ParametrizacaoConsultaPreco atualizada = parametrizacaoService.atualizar(id, parametrizacao);
            return ApiResponse.success("Parametrização atualizada com sucesso", atualizada);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao atualizar parametrização: " + e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public ApiResponse<Void> excluir(@PathParam("id") Long id) {
        try {
            parametrizacaoService.excluir(id);
            return ApiResponse.success("Parametrização excluída com sucesso", null);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao excluir parametrização: " + e.getMessage());
        }
    }
}
