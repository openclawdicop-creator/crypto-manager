package org.acme.ws;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v3/depth")
@RegisterRestClient(configKey = "binance-api")
@Produces(MediaType.APPLICATION_JSON)
public interface BinanceApi {

    @GET
    String consultarProfundidade(@QueryParam("symbol") String symbol, @QueryParam("limit") Integer limit);
}
