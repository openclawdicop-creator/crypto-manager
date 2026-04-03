package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.entity.Proxy;
import org.acme.service.ProxyService;

import java.util.List;

@Path("/api/proxies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ProxyResource {

    @Inject
    ProxyService proxyService;

    @GET
    public ApiResponse<List<Proxy>> listarTodos() {
        try {
            List<Proxy> proxies = proxyService.listarTodos();
            return ApiResponse.success(proxies);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar proxies: " + e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public ApiResponse<Proxy> buscarPorId(@PathParam("id") Long id) {
        try {
            return proxyService.buscarPorId(id)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error("Proxy não encontrado"));
        } catch (Exception e) {
            return ApiResponse.error("Erro ao buscar proxy: " + e.getMessage());
        }
    }

    @POST
    public ApiResponse<Proxy> criar(Proxy proxy) {
        try {
            Proxy criado = proxyService.criar(proxy);
            return ApiResponse.success("Proxy criado com sucesso", criado);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao criar proxy: " + e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    public ApiResponse<Proxy> atualizar(@PathParam("id") Long id, Proxy proxy) {
        try {
            Proxy atualizado = proxyService.atualizar(id, proxy);
            return ApiResponse.success("Proxy atualizado com sucesso", atualizado);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao atualizar proxy: " + e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public ApiResponse<Void> excluir(@PathParam("id") Long id) {
        try {
            proxyService.excluir(id);
            return ApiResponse.success("Proxy excluído com sucesso", null);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao excluir proxy: " + e.getMessage());
        }
    }
}
