package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.entity.Exchange;
import org.acme.service.ExchangeService;

import java.util.List;

@Path("/api/exchanges")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ExchangeResource {

    @Inject
    ExchangeService exchangeService;

    @GET
    public ApiResponse<List<Exchange>> listarTodos() {
        try {
            List<Exchange> exchanges = exchangeService.listarTodos();
            return ApiResponse.success(exchanges);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar exchanges: " + e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public ApiResponse<Exchange> buscarPorId(@PathParam("id") Long id) {
        try {
            return exchangeService.buscarPorId(id)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error("Exchange não encontrada"));
        } catch (Exception e) {
            return ApiResponse.error("Erro ao buscar exchange: " + e.getMessage());
        }
    }

    @POST
    public ApiResponse<Exchange> criar(Exchange exchange) {
        try {
            Exchange criada = exchangeService.criar(exchange);
            return ApiResponse.success("Exchange criada com sucesso", criada);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao criar exchange: " + e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    public ApiResponse<Exchange> atualizar(@PathParam("id") Long id, Exchange exchange) {
        try {
            Exchange atualizada = exchangeService.atualizar(id, exchange);
            return ApiResponse.success("Exchange atualizada com sucesso", atualizada);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao atualizar exchange: " + e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public ApiResponse<Void> excluir(@PathParam("id") Long id) {
        try {
            exchangeService.excluir(id);
            return ApiResponse.success("Exchange excluída com sucesso", null);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao excluir exchange: " + e.getMessage());
        }
    }
}
