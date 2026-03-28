package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.entity.AtivoFinanceiro;
import org.acme.service.AtivoFinanceiroService;

import java.util.List;

@Path("/api/ativos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AtivoFinanceiroResource {

    @Inject
    AtivoFinanceiroService ativoFinanceiroService;

    @GET
    public ApiResponse<List<AtivoFinanceiro>> listarTodos() {
        try {
            List<AtivoFinanceiro> ativos = ativoFinanceiroService.listarTodos();
            return ApiResponse.success(ativos);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar ativos financeiros: " + e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public ApiResponse<AtivoFinanceiro> buscarPorId(@PathParam("id") Long id) {
        try {
            return ativoFinanceiroService.buscarPorId(id)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error("Ativo financeiro não encontrado"));
        } catch (Exception e) {
            return ApiResponse.error("Erro ao buscar ativo financeiro: " + e.getMessage());
        }
    }

    @POST
    public ApiResponse<AtivoFinanceiro> criar(AtivoFinanceiro ativoFinanceiro) {
        try {
            AtivoFinanceiro criado = ativoFinanceiroService.criar(ativoFinanceiro);
            return ApiResponse.success("Ativo financeiro criado com sucesso", criado);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao criar ativo financeiro: " + e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    public ApiResponse<AtivoFinanceiro> atualizar(@PathParam("id") Long id, AtivoFinanceiro ativoFinanceiro) {
        try {
            AtivoFinanceiro atualizado = ativoFinanceiroService.atualizar(id, ativoFinanceiro);
            return ApiResponse.success("Ativo financeiro atualizado com sucesso", atualizado);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao atualizar ativo financeiro: " + e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public ApiResponse<Void> excluir(@PathParam("id") Long id) {
        try {
            ativoFinanceiroService.excluir(id);
            return ApiResponse.success("Ativo financeiro excluído com sucesso", null);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao excluir ativo financeiro: " + e.getMessage());
        }
    }
}
