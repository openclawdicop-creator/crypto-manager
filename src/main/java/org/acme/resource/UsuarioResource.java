package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.entity.Usuario;
import org.acme.service.UsuarioService;

import java.util.List;

@Path("/api/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    @Inject
    UsuarioService usuarioService;

    @GET
    public ApiResponse<List<Usuario>> listarTodos() {
        try {
            List<Usuario> usuarios = usuarioService.listarTodos();
            return ApiResponse.success(usuarios);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar usuários: " + e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public ApiResponse<Usuario> buscarPorId(@PathParam("id") Long id) {
        try {
            return usuarioService.buscarPorId(id)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error("Usuário não encontrado"));
        } catch (Exception e) {
            return ApiResponse.error("Erro ao buscar usuário: " + e.getMessage());
        }
    }

    @POST
    public ApiResponse<Usuario> criar(Usuario usuario) {
        try {
            Usuario criado = usuarioService.criar(usuario);
            return ApiResponse.success("Usuário criado com sucesso", criado);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao criar usuário: " + e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    public ApiResponse<Usuario> atualizar(@PathParam("id") Long id, Usuario usuario) {
        try {
            Usuario atualizado = usuarioService.atualizar(id, usuario);
            return ApiResponse.success("Usuário atualizado com sucesso", atualizado);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao atualizar usuário: " + e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public ApiResponse<Void> excluir(@PathParam("id") Long id) {
        try {
            usuarioService.excluir(id);
            return ApiResponse.success("Usuário excluído com sucesso", null);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao excluir usuário: " + e.getMessage());
        }
    }
}
