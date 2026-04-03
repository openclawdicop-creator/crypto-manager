package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.SQLExecutionRequest;
import org.acme.dto.SQLExecutionResult;
import org.acme.service.SQLService;

@Path("/api/sql")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class SQLResource {

    @Inject
    SQLService sqlService;

    @POST
    @Path("/execute")
    public ApiResponse<SQLExecutionResult> execute(SQLExecutionRequest request) {
        try {
            SQLExecutionResult result = sqlService.execute(request != null ? request.sql : null);
            String message = "SQL executado com sucesso.";
            if ("RESULT_SET".equals(result.executionType)) {
                message = "Consulta executada com sucesso.";
            }
            return ApiResponse.success(message, result);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao executar SQL: " + e.getMessage());
        }
    }
}
