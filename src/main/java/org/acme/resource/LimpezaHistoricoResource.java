package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.service.LimpezaHistoricoService;
import org.jboss.logging.Logger;

import java.util.Map;

@Path("/api/agendamentos/limpeza")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class LimpezaHistoricoResource {

    private static final Logger LOG = Logger.getLogger(LimpezaHistoricoResource.class);

    @Inject
    LimpezaHistoricoService limpezaService;

    /**
     * Executa a limpeza manualmente
     */
    @POST
    @Path("/executar")
    public ApiResponse<Map<String, Object>> executarLimpeza() {
        try {
            LOG.info("Execução manual de limpeza solicitada");
            Map<String, Object> resultado = limpezaService.limparHistoricosAntigos();
            if ((Boolean) resultado.get("sucesso")) {
                return ApiResponse.success("Limpeza executada com sucesso", resultado);
            } else {
                return ApiResponse.error((String) resultado.get("erro"));
            }
        } catch (Exception e) {
            LOG.error("Erro ao executar limpeza manual: " + e.getMessage());
            return ApiResponse.error("Erro ao executar limpeza: " + e.getMessage());
        }
    }

    /**
     * Retorna o status do scheduler de limpeza
     */
    @GET
    @Path("/status")
    public ApiResponse<Map<String, Object>> getStatus() {
        try {
            Map<String, Object> status = limpezaService.getStatus();
            return ApiResponse.success(status);
        } catch (Exception e) {
            LOG.error("Erro ao buscar status: " + e.getMessage());
            return ApiResponse.error("Erro ao buscar status: " + e.getMessage());
        }
    }

    /**
     * Pausa o scheduler de limpeza
     */
    @POST
    @Path("/pausar")
    public ApiResponse<Map<String, Object>> pausarScheduler() {
        try {
            limpezaService.setSchedulerAtivo(false);
            Map<String, Object> response = limpezaService.getStatus();
            return ApiResponse.success("Scheduler de limpeza pausado", response);
        } catch (Exception e) {
            LOG.error("Erro ao pausar scheduler: " + e.getMessage());
            return ApiResponse.error("Erro ao pausar scheduler: " + e.getMessage());
        }
    }

    /**
     * Inicia/reativa o scheduler de limpeza
     */
    @POST
    @Path("/iniciar")
    public ApiResponse<Map<String, Object>> iniciarScheduler() {
        try {
            limpezaService.setSchedulerAtivo(true);
            Map<String, Object> response = limpezaService.getStatus();
            return ApiResponse.success("Scheduler de limpeza ativado", response);
        } catch (Exception e) {
            LOG.error("Erro ao iniciar scheduler: " + e.getMessage());
            return ApiResponse.error("Erro ao iniciar scheduler: " + e.getMessage());
        }
    }
}
