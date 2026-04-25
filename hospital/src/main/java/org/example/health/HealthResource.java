package org.example.health;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/health")
@Produces(MediaType.TEXT_PLAIN)
public class HealthResource {

    @GET
    public String health() {
        return "OK";
    }
}






