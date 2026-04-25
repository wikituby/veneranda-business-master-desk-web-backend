package org.example.lab.generalReport.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.lab.generalReport.services.GeneralLabReportService;
import org.example.lab.generalReport.services.Payloads.requests.GeneralLabReportUpdateRequest;
import org.example.lab.generalReport.services.Payloads.responses.GeneralLabReportDTO;

@Path("diagnostics-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "lab Management Module", description = "lab Management Module")
public class GeneralLabReportController {

    @Inject
    GeneralLabReportService generalLabReportService;

    @PUT
    @Path("update-general-lab-report/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "Update general lab report", description = "Update general lab report")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = GeneralLabReportDTO.class)))
    public Response updateGeneralLabReport(@PathParam("id") Long id, GeneralLabReportUpdateRequest request) {
        return generalLabReportService.updateGeneralLabReportById(id, request);
    }

    @GET
    @Path("get-general-lab-report-by-request-id/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "get-general-lab-report-by-request-id", description = "get-general-lab-report-by-request-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response getLabReportForRequestId(@PathParam("id") Long procedureRequestedId) {
        return generalLabReportService.getLabReportByRequestId(procedureRequestedId);
    }
}

























