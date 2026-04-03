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
import org.acme.entity.HistoricoCotacao;
import org.acme.entity.ParametrizacaoConsultaPreco;
import org.acme.service.CotacaoService;
import org.acme.service.ParametrizacaoConsultaPrecoService;

import java.util.List;

@Path("/api/parametrizacoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ParametrizacaoConsultaPrecoResource {

    @Inject
    ParametrizacaoConsultaPrecoService parametrizacaoService;

    @Inject
    CotacaoService cotacaoService;

    @GET
    public ApiResponse<List<ParametrizacaoConsultaPreco>> listarTodos() {
        try {
            List<ParametrizacaoConsultaPreco> parametrizacoes = parametrizacaoService.listarTodos();
            return ApiResponse.success(parametrizacoes);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar parametrizacoes: " + e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public ApiResponse<ParametrizacaoConsultaPreco> buscarPorId(@PathParam("id") Long id) {
        try {
            return parametrizacaoService.buscarPorId(id)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error("Parametrizacao nao encontrada"));
        } catch (Exception e) {
            return ApiResponse.error("Erro ao buscar parametrizacao: " + e.getMessage());
        }
    }

    @GET
    @Path("/ativas")
    public ApiResponse<List<ParametrizacaoConsultaPreco>> listarAtivas() {
        try {
            List<ParametrizacaoConsultaPreco> parametrizacoes = parametrizacaoService.listarPorAtiva(true);
            return ApiResponse.success(parametrizacoes);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar parametrizacoes ativas: " + e.getMessage());
        }
    }

    @POST
    public ApiResponse<ParametrizacaoConsultaPreco> criar(ParametrizacaoConsultaPreco parametrizacao) {
        try {
            ParametrizacaoConsultaPreco criada = parametrizacaoService.criar(parametrizacao);
            return ApiResponse.success("Parametrizacao criada com sucesso", criada);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao criar parametrizacao: " + e.getMessage());
        }
    }

    @POST
    @Path("/consultar-preco")
    public ApiResponse<HistoricoCotacao> consultarPreco(ParametrizacaoConsultaPreco parametrizacao) {
        try {
            if (parametrizacao == null || parametrizacao.id == null) {
                return ApiResponse.error("A parametrizacao deve possuir ID para executar a consulta.");
            }

            HistoricoCotacao historicoCotacao = cotacaoService.processarConsulta(parametrizacao);
            return ApiResponse.success("Consulta de preco realizada com sucesso", historicoCotacao);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao consultar preco: " + e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    public ApiResponse<ParametrizacaoConsultaPreco> atualizar(@PathParam("id") Long id,
                                                              ParametrizacaoConsultaPreco parametrizacao) {
        try {
            ParametrizacaoConsultaPreco atualizada = parametrizacaoService.atualizar(id, parametrizacao);
            return ApiResponse.success("Parametrizacao atualizada com sucesso", atualizada);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Erro ao atualizar parametrizacao: " + e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public ApiResponse<Void> excluir(@PathParam("id") Long id) {
        try {
            parametrizacaoService.excluir(id);
            return ApiResponse.success("Parametrizacao excluida com sucesso", null);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao excluir parametrizacao: " + e.getMessage());
        }
    }

    @POST
    @Path("/ativar-todas")
    public ApiResponse<List<ParametrizacaoConsultaPreco>> ativarTodas() {
        try {
            List<ParametrizacaoConsultaPreco> atualizadas = parametrizacaoService.ativarTodas();
            return ApiResponse.success("Todas as parametrizacoes foram ativadas", atualizadas);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao ativar todas as parametrizacoes: " + e.getMessage());
        }
    }

    @POST
    @Path("/desativar-todas")
    public ApiResponse<List<ParametrizacaoConsultaPreco>> desativarTodas() {
        try {
            List<ParametrizacaoConsultaPreco> atualizadas = parametrizacaoService.desativarTodas();
            return ApiResponse.success("Todas as parametrizacoes foram desativadas", atualizadas);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao desativar todas as parametrizacoes: " + e.getMessage());
        }
    }
}
