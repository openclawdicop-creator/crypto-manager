package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.entity.Rede;
import org.acme.service.RedeService;

import java.util.List;

@Path("/api/redes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RedeResource {

    @Inject
    RedeService redeService;

    @GET
    public ApiResponse<List<Rede>> listarTodos() {
        try {
            List<Rede> redes = redeService.listarTodos();
            return ApiResponse.success(redes);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar redes: " + e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public ApiResponse<Rede> buscarPorId(@PathParam("id") Long id) {
        try {
            return redeService.buscarPorId(id)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error("Rede não encontrada"));
        } catch (Exception e) {
            return ApiResponse.error("Erro ao buscar rede: " + e.getMessage());
        }
    }

    @POST
    public ApiResponse<Rede> criar(Rede rede) {
        try {
            Rede criada = redeService.criar(rede);
            return ApiResponse.success("Rede criada com sucesso", criada);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao criar rede: " + e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    public ApiResponse<Rede> atualizar(@PathParam("id") Long id, Rede rede) {
        try {
            Rede atualizada = redeService.atualizar(id, rede);
            return ApiResponse.success("Rede atualizada com sucesso", atualizada);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao atualizar rede: " + e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public ApiResponse<Void> excluir(@PathParam("id") Long id) {
        try {
            redeService.excluir(id);
            return ApiResponse.success("Rede excluída com sucesso", null);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao excluir rede: " + e.getMessage());
        }
    }
}
