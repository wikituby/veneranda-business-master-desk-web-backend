package org.example.support.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.support.services.WebsiteContactService;
import org.example.support.services.payloads.WebsiteContactSubmitRequest;

import java.util.Map;

@Path("support/website-contact")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Website Contact", description = "Public website contact messages inbox")
public class WebsiteContactController {

    @Inject
    WebsiteContactService websiteContactService;

    @POST
    @Path("/public")
    @Operation(summary = "Submit a public website contact message (no login required)")
    public Response submit(WebsiteContactSubmitRequest request) {
        return websiteContactService.submit(request);
    }

    @GET
    @Authenticated
    @Operation(summary = "List all website contact messages")
    public Response listAll() {
        return websiteContactService.listAll();
    }

    @PUT
    @Authenticated
    @Path("/{id}/status")
    @Operation(summary = "Update website contact message status")
    public Response updateStatus(@PathParam("id") Long id, Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        return websiteContactService.updateStatus(id, status);
    }
}
