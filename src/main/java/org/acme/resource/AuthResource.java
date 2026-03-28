package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.acme.entity.Usuario;
import org.acme.service.JwtService;
import org.acme.service.UsuarioService;

import java.util.HashMap;
import java.util.Map;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    UsuarioService usuarioService;
    
    @Inject
    JwtService jwtService;

    @POST
    @Path("/login")
    public ApiResponse<Map<String, Object>> login(LoginRequest request) {
        try {
            boolean valido = usuarioService.validarCredenciais(request.username, request.password);
            if (!valido) {
                return ApiResponse.error("Username ou senha inválidos");
            }

            Usuario usuario = usuarioService.buscarPorUsername(request.username);
            if (!usuario.ativo) {
                return ApiResponse.error("Usuário inativo");
            }

            String token = jwtService.generateToken(usuario);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", usuario.username);
            data.put("email", usuario.email);

            return ApiResponse.success("Login realizado com sucesso", data);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao realizar login: " + e.getMessage());
        }
    }

    @POST
    @Path("/reset-admin")
    @Transactional
    public ApiResponse<Void> resetAdmin() {
        try {
            Usuario admin = usuarioService.buscarPorUsername("admin");
            if (admin != null) {
                admin.password = org.mindrot.jbcrypt.BCrypt.hashpw("admin1234", org.mindrot.jbcrypt.BCrypt.gensalt());
                admin.persist();
                return ApiResponse.success("Senha do admin resetada", null);
            }
            // Cria o admin se não existir
            admin = new Usuario();
            admin.username = "admin";
            admin.password = org.mindrot.jbcrypt.BCrypt.hashpw("admin1234", org.mindrot.jbcrypt.BCrypt.gensalt());
            admin.email = "admin@cryptomanager.com";
            admin.ativo = true;
            admin.persist();
            return ApiResponse.success("Admin criado com sucesso", null);
        } catch (Exception e) {
            return ApiResponse.error("Erro: " + e.getMessage());
        }
    }

    @GET
    @Path("/check-admin")
    public ApiResponse<Boolean> checkAdmin() {
        try {
            Usuario admin = usuarioService.buscarPorUsername("admin");
            return ApiResponse.success("Admin verificado", admin != null);
        } catch (Exception e) {
            return ApiResponse.error("Erro: " + e.getMessage());
        }
    }

    @POST
    @Path("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success("Logout realizado com sucesso", null);
    }

    @GET
    @Path("/me")
    public ApiResponse<Map<String, Object>> getMe(@Context SecurityContext securityContext) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            Usuario usuario = usuarioService.buscarPorUsername(username);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", usuario.id);
            data.put("username", usuario.username);
            data.put("email", usuario.email);
            data.put("ativo", usuario.ativo);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao buscar usuário: " + e.getMessage());
        }
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }
}
