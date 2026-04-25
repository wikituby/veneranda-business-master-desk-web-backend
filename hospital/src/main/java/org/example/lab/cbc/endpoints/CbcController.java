package org.example.lab.cbc.endpoints;

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
import org.example.lab.cbc.services.CbcService;
import org.example.lab.cbc.services.Payloads.requests.CbcUpdateRequest;
import org.example.lab.cbc.services.Payloads.responses.CbcDTO;

@Path("diagnostics-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "lab Management Module", description = "lab Management Module")
public class CbcController {

    @Inject
    CbcService cbcService;

    @PUT
    @Path("update-cbc-report/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "Update cbc report", description = "Update cbc report")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CbcDTO.class)))
    public Response updateCbcReport(@PathParam("id") Long id, CbcUpdateRequest request) {
        return cbcService.updateCbcReportById(id, request);
    }

    @GET
    @Path("get-cbc-report-by-request-id/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "get-cbc-report-by-request-id", description = "get-cbc-report-by-request-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response getLabReportForRequestId(@PathParam("id") Long procedureRequestedId) {
        return cbcService.getLabReportByRequestId(procedureRequestedId);
    }
}
























