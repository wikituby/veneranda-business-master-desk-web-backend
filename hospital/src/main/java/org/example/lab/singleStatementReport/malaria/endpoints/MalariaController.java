package org.example.lab.singleStatementReport.malaria.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import org.example.lab.singleStatementReport.malaria.services.MalariaService;
import org.example.lab.singleStatementReport.malaria.services.Payloads.requests.MalariaUpdateRequest;
import org.example.lab.singleStatementReport.malaria.services.Payloads.responses.MalariaDTO;


@Path("diagnostics-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "lab Management Module", description = "lab Management Module")

public class MalariaController {
    @Inject
    MalariaService malariaService;


    @PUT
    @Path("update-malaria-report/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "Update malaria report", description = "Update malaria report")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = MalariaDTO.class)))
    public Response updateScanReport(@PathParam("id") Long id, MalariaUpdateRequest request){
        return malariaService.updateMalariaReportById(id, request);
    }
/*
    @GET
    @Path("scan-generate-pdf/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "scan pdf", description = "scan pdf download")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response generateAndReturnScanPdf(@PathParam("id") Long procedureRequestedId) {
        return generalUsService.generateAndReturnScanReportPdf(procedureRequestedId);
    }*/

    @GET
    @Path("get-malaria-report-by-request-id/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "get-malaria-report-by-request-id", description = "get-malaria-report-by-request-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response getLabReportForRequestId(@PathParam("id") Long procedureRequestedId) {
        return malariaService.getLabReportByRequestId(procedureRequestedId);
    }

    /*
    @GET
    @Transactional
    @Path("/get-all-generalUs")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all generalUs", description = "How to Retrieve a list of all generalUs")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = GeneralUsDTO.class, type = SchemaType.ARRAY)))
    public Response getAllGeneralUs() {
        List<GeneralUsDTO> generalUs = generalUsService.getAllGeneralUs();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, generalUs)).build();
    }*/
}






