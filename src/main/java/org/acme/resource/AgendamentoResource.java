package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.entity.Agendamento;
import org.acme.service.AgendamentoService;

import java.util.List;

@Path("/api/agendamentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgendamentoResource {

    @Inject
    AgendamentoService agendamentoService;

    @GET
    public ApiResponse<List<Agendamento>> listar() {
        try {
            List<Agendamento> agendamentos = agendamentoService.listarTodos();
            return ApiResponse.success(agendamentos);
        } catch (Exception e) {
            return ApiResponse.error("Erro ao listar agendamentos: " + e.getMessage());
        }
    }

    @POST
    public Response criar(Agendamento agendamento) {
        Agendamento criado = agendamentoService.criar(agendamento);
        return Response.ok(criado).build();
    }

    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") Long id, Agendamento agendamento) {
        Agendamento atualizado = agendamentoService.atualizar(id, agendamento);
        return Response.ok(atualizado).build();
    }

    @POST
    @Path("/{id}/iniciar")
    public Response iniciar(@PathParam("id") Long id) {
        Agendamento agendamento = Agendamento.findById(id);
        if (agendamento != null) {
            agendamento.ativo = true;
            agendamentoService.atualizar(id, agendamento);
            return Response.ok(agendamento).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/{id}/parar")
    public Response parar(@PathParam("id") Long id) {
        Agendamento agendamento = Agendamento.findById(id);
        if (agendamento != null) {
            agendamento.ativo = false;
            agendamentoService.atualizar(id, agendamento);
            return Response.ok(agendamento).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
