package org.acme.resource;

import io.quarkus.qute.Template;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@PermitAll
public class PageResource {

    @Inject
    Template dashboard;
    
    @Inject
    Template login;
    
    @Inject
    Template exchanges;
    
    @Inject
    Template redes;
    
    @Inject
    Template ativos;
    
    @Inject
    Template parametrizacoes;
    
    @Inject
    Template historicos;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response home() {
        return Response.seeOther(java.net.URI.create("/dashboard")).build();
    }

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public Response loginPage() {
        return Response.ok(login.instance().render()).build();
    }

    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public Response dashboardPage() {
        return Response.ok(dashboard.instance().render()).build();
    }

    @GET
    @Path("/exchanges")
    @Produces(MediaType.TEXT_HTML)
    public Response exchangesPage() {
        return Response.ok(exchanges.instance().render()).build();
    }

    @GET
    @Path("/redes")
    @Produces(MediaType.TEXT_HTML)
    public Response redesPage() {
        return Response.ok(redes.instance().render()).build();
    }

    @GET
    @Path("/ativos")
    @Produces(MediaType.TEXT_HTML)
    public Response ativosPage() {
        return Response.ok(ativos.instance().render()).build();
    }

    @GET
    @Path("/parametrizacoes")
    @Produces(MediaType.TEXT_HTML)
    public Response parametrizacoesPage() {
        return Response.ok(parametrizacoes.instance().render()).build();
    }

    @GET
    @Path("/historicos")
    @Produces(MediaType.TEXT_HTML)
    public Response historicosPage() {
        return Response.ok(historicos.instance().render()).build();
    }
}
