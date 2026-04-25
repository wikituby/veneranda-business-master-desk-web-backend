package org.example.lab.parasitologyStool.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.lab.parasitologyStool.services.ParasitologyStoolService;
import org.example.lab.parasitologyStool.services.Payloads.requests.ParasitologyStoolUpdateRequest;
import org.example.lab.parasitologyStool.services.Payloads.responses.ParasitologyStoolDTO;

@Path("diagnostics-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "lab Management Module", description = "lab Management Module")
public class ParasitologyStoolController {

    @Inject
    ParasitologyStoolService parasitologyStoolService;

    @PUT
    @Path("update-parasitology-stool-report/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "Update parasitology stool report", description = "Update parasitology stool report")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ParasitologyStoolDTO.class)))
    public Response updateParasitologyStoolReport(@PathParam("id") Long id, ParasitologyStoolUpdateRequest request) {
        return parasitologyStoolService.updateParasitologyStoolReportById(id, request);
    }

    @GET
    @Path("get-parasitology-stool-report-by-request-id/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "get-parasitology-stool-report-by-request-id", description = "get-parasitology-stool-report-by-request-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response getLabReportForRequestId(@PathParam("id") Long procedureRequestedId) {
        return parasitologyStoolService.getLabReportByRequestId(procedureRequestedId);
    }
}
























