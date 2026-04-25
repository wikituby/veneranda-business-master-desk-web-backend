package org.example.lab.urinalysis.endpoints;

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
import org.example.lab.urinalysis.services.UrinalysisService;
import org.example.lab.urinalysis.services.Payloads.requests.UrinalysisUpdateRequest;
import org.example.lab.urinalysis.services.Payloads.responses.UrinalysisDTO;

@Path("diagnostics-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "lab Management Module", description = "lab Management Module")
public class UrinalysisController {

    @Inject
    UrinalysisService urinalysisService;

    @PUT
    @Path("update-urinalysis-report/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "Update urinalysis report", description = "Update urinalysis report")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = UrinalysisDTO.class)))
    public Response updateUrinalysisReport(@PathParam("id") Long id, UrinalysisUpdateRequest request) {
        return urinalysisService.updateUrinalysisReportById(id, request);
    }

    @GET
    @Path("get-urinalysis-report-by-request-id/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "get-urinalysis-report-by-request-id", description = "get-urinalysis-report-by-request-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response getLabReportForRequestId(@PathParam("id") Long procedureRequestedId) {
        return urinalysisService.getLabReportByRequestId(procedureRequestedId);
    }
}
























