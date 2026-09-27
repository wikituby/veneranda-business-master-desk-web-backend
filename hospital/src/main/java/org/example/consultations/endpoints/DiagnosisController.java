package org.example.consultations.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.services.DiagnosisService;
import org.example.consultations.services.payloads.requests.DiagnosisRequest;
import org.example.consultations.services.payloads.responses.DiagnosisDTO;

import java.util.List;

@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Client Management Module - diagnoses", description = "Consultation diagnoses")
public class DiagnosisController {

    @Inject
    DiagnosisService diagnosisService;

    @POST
    @Path("create-diagnosis/{visitId}")
    @Transactional
    @Operation(summary = "Create diagnosis for a visit consultation")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = DiagnosisDTO.class)))
    public Response createDiagnosis(@PathParam("visitId") Long visitId, DiagnosisRequest request) {
        return diagnosisService.createDiagnosisForVisit(visitId, request);
    }

    @PUT
    @Path("update-diagnosis/{id}")
    @Transactional
    @Operation(summary = "Update diagnosis")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = DiagnosisDTO.class)))
    public Response updateDiagnosis(@PathParam("id") Long id, DiagnosisRequest request) {
        return diagnosisService.updateDiagnosis(id, request);
    }

    @DELETE
    @Path("delete-diagnosis/{id}")
    @Transactional
    @Operation(summary = "Delete diagnosis (unlinks treatments, does not delete them)")
    public Response deleteDiagnosis(@PathParam("id") Long id) {
        return diagnosisService.deleteDiagnosis(id);
    }

    @GET
    @Path("get-diagnoses-by-visit/{visitId}")
    @Operation(summary = "List diagnoses for a visit")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = DiagnosisDTO.class, type = SchemaType.ARRAY)))
    public Response getDiagnosesByVisit(@PathParam("visitId") Long visitId) {
        List<DiagnosisDTO> diagnoses = diagnosisService.listByVisitId(visitId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, diagnoses)).build();
    }

    @GET
    @Path("get-diagnosis/{id}")
    @Operation(summary = "Get diagnosis by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = DiagnosisDTO.class)))
    public Response getDiagnosis(@PathParam("id") Long id) {
        return diagnosisService.getById(id);
    }
}
