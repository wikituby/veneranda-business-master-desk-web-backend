package org.example.diagnostics.ultrasoundScan.generalUs.endpoints;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.client.services.payloads.requests.PatientUpdateRequest;
import org.example.client.services.payloads.responses.dtos.PatientDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.services.payloads.responses.ConsultationDTO;
import org.example.diagnostics.ultrasoundScan.generalUs.services.GeneralUsService;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsRequest;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsUpdateRequest;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.responses.GeneralUsDTO;

import java.util.List;


@Path("diagnostics-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ultrasound Scan Management Module", description = "Ultrasound Scan Management")

public class GeneralUsController {


    @Inject
    GeneralUsService generalUsService;


    @PUT
    @Path("update-scan-report/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "Update scan report", description = "Update scan report")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = GeneralUsDTO.class)))
    public Response updateScanReport(@PathParam("id") Long id, GeneralUsUpdateRequest request){
        return generalUsService.updateScanReportById(id, request);
    }

    @GET
    @Path("scan-generate-pdf/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "scan pdf", description = "scan pdf download")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response generateAndReturnScanPdf(@PathParam("id") Long procedureRequestedId) {
        return generalUsService.generateAndReturnScanReportPdf(procedureRequestedId);
    }


    @GET
    @Path("get-scan-report-by-request-id/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER","md"})
    @Transactional
    @Operation(summary = "get-scan-report-by-request-id", description = "get-scan-report-by-request-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response getScanReportForRequestId(@PathParam("id") Long procedureRequestedId) {
        return generalUsService.getScanReportByRequestId(procedureRequestedId);
    }

    @GET
    @Transactional
    @Path("/get-all-generalUs")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all generalUs", description = "How to Retrieve a list of all generalUs")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = GeneralUsDTO.class, type = SchemaType.ARRAY)))
    public Response getAllGeneralUs() {
        List<GeneralUsDTO> generalUs = generalUsService.getAllGeneralUs();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, generalUs)).build();
    }
}






