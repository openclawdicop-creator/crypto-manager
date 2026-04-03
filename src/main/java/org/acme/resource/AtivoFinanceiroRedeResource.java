package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.entity.AtivoFinanceiroRede;
import org.acme.service.AtivoFinanceiroRedeService;

import java.util.List;

@Path("/api/ativos-redes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class AtivoFinanceiroRedeResource {

    @Inject
    AtivoFinanceiroRedeService ativoFinanceiroRedeService;

    @GET
    public ApiResponse<List<AtivoFinanceiroRede>> listarTodos() {
        try {
            List<AtivoFinanceiroRede> configuracoes = ativoFinanceiroRedeService.listarTodos();
            return ApiResponse.success(configuracoes);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar configuracoes ativo rede: " + e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public ApiResponse<AtivoFinanceiroRede> buscarPorId(@PathParam("id") Long id) {
        try {
            return ativoFinanceiroRedeService.buscarPorId(id)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error("Configuracao ativo rede nao encontrada"));
        } catch (Exception e) {
            return ApiResponse.error("Erro ao buscar configuracao ativo rede: " + e.getMessage());
        }
    }

    @POST
    public ApiResponse<AtivoFinanceiroRede> criar(AtivoFinanceiroRede ativoFinanceiroRede) {
        try {
            AtivoFinanceiroRede criada = ativoFinanceiroRedeService.criar(ativoFinanceiroRede);
            return ApiResponse.success("Configuracao ativo rede criada com sucesso", criada);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao criar configuracao ativo rede: " + e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    public ApiResponse<AtivoFinanceiroRede> atualizar(@PathParam("id") Long id, AtivoFinanceiroRede ativoFinanceiroRede) {
        try {
            AtivoFinanceiroRede atualizada = ativoFinanceiroRedeService.atualizar(id, ativoFinanceiroRede);
            return ApiResponse.success("Configuracao ativo rede atualizada com sucesso", atualizada);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao atualizar configuracao ativo rede: " + e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public ApiResponse<Void> excluir(@PathParam("id") Long id) {
        try {
            ativoFinanceiroRedeService.excluir(id);
            return ApiResponse.success("Configuracao ativo rede excluida com sucesso", null);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao excluir configuracao ativo rede: " + e.getMessage());
        }
    }
}
