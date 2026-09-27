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
import org.example.consultations.services.DiagnosisTypeService;
import org.example.consultations.services.payloads.requests.DiagnosisTypeRequest;
import org.example.consultations.services.payloads.responses.DiagnosisTypeDTO;

import java.util.List;

@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Client Management Module - Diagnosis Types", description = "Reusable diagnosis name catalog")
public class DiagnosisTypeController {

    @Inject
    DiagnosisTypeService diagnosisTypeService;

    @POST
    @Path("/create-new-diagnosis-type")
    @Transactional
    @Operation(summary = "Create a diagnosis type")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = DiagnosisTypeDTO.class)))
    public Response create(DiagnosisTypeRequest request) {
        return diagnosisTypeService.create(request);
    }

    @PUT
    @Path("/update-diagnosis-type/{id}")
    @Transactional
    @Operation(summary = "Update a diagnosis type")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = DiagnosisTypeDTO.class)))
    public Response update(@PathParam("id") Long id, DiagnosisTypeRequest request) {
        return diagnosisTypeService.update(id, request);
    }

    @DELETE
    @Path("/delete-diagnosis-type/{id}")
    @Transactional
    @Operation(summary = "Delete a diagnosis type")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ResponseMessage.class)))
    public Response delete(@PathParam("id") Long id) {
        return diagnosisTypeService.delete(id);
    }

    @GET
    @Path("/get-all-diagnosis-types")
    @Transactional
    @Operation(summary = "Get all diagnosis types")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = DiagnosisTypeDTO.class, type = SchemaType.ARRAY)))
    public Response getAll() {
        List<DiagnosisTypeDTO> rows = diagnosisTypeService.listAll();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, rows)).build();
    }

    @GET
    @Path("/get-diagnosis-type/{id}")
    @Transactional
    @Operation(summary = "Get diagnosis type by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = DiagnosisTypeDTO.class)))
    public Response getById(@PathParam("id") Long id) {
        return diagnosisTypeService.getById(id);
    }
}
