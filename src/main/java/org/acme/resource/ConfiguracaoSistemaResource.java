package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.entity.ConfiguracaoSistema;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

@Path("/api/parametros")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ConfiguracaoSistemaResource {

    private static final Logger LOG = Logger.getLogger(ConfiguracaoSistemaResource.class);
    private static final String CHAVE_DIAS_MANUTENCAO = "DIAS_MANUTENCAO_HISTORICO";

    @GET
    @Path("/dias-manutencao")
    public ApiResponse<Map<String, Object>> getDiasManutencao() {
        try {
            ConfiguracaoSistema config = ConfiguracaoSistema.find("chave", CHAVE_DIAS_MANUTENCAO).firstResult();
            int dias = 6; // padrão
            if (config != null) {
                dias = Integer.parseInt(config.valor);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("chave", CHAVE_DIAS_MANUTENCAO);
            response.put("valor", dias);
            return ApiResponse.success(response);
        } catch (Exception e) {
            LOG.error("Erro ao buscar dias de manutenção: " + e.getMessage());
            return ApiResponse.error("Erro ao buscar parâmetro: " + e.getMessage());
        }
    }

    @PUT
    @Path("/dias-manutencao")
    @Transactional
    public ApiResponse<Map<String, Object>> setDiasManutencao(Map<String, Object> body) {
        try {
            int dias = ((Number) body.get("valor")).intValue();
            if (dias < 1 || dias > 365) {
                return ApiResponse.error("Dias deve estar entre 1 e 365");
            }

            ConfiguracaoSistema config = ConfiguracaoSistema.find("chave", CHAVE_DIAS_MANUTENCAO).firstResult();
            if (config == null) {
                config = new ConfiguracaoSistema(CHAVE_DIAS_MANUTENCAO, String.valueOf(dias));
                config.persist();
            } else {
                config.valor = String.valueOf(dias);
                config.persist();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("chave", CHAVE_DIAS_MANUTENCAO);
            response.put("valor", dias);
            LOG.info("Parâmetro dias de manutenção atualizado para: " + dias);
            return ApiResponse.success("Parâmetro atualizado com sucesso", response);
        } catch (Exception e) {
            LOG.error("Erro ao atualizar dias de manutenção: " + e.getMessage());
            return ApiResponse.error("Erro ao atualizar parâmetro: " + e.getMessage());
        }
    }
}
