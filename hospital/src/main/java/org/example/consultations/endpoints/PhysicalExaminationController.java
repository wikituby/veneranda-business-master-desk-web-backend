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
import org.example.consultations.services.PhysicalExaminationService;
import org.example.consultations.services.payloads.requests.PhysicalExaminationRequest;
import org.example.consultations.services.payloads.responses.PhysicalExaminationDTO;

import java.util.List;

@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Client Management Module - physical examinations", description = "Structured physical examinations")
public class PhysicalExaminationController {

    @Inject
    PhysicalExaminationService physicalExaminationService;

    @POST
    @Path("create-physical-examination/{visitId}")
    @Transactional
    @Operation(summary = "Create physical examination for a visit consultation")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PhysicalExaminationDTO.class)))
    public Response create(@PathParam("visitId") Long visitId, PhysicalExaminationRequest request) {
        return physicalExaminationService.createForVisit(visitId, request);
    }

    @PUT
    @Path("update-physical-examination/{id}")
    @Transactional
    @Operation(summary = "Update physical examination")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PhysicalExaminationDTO.class)))
    public Response update(@PathParam("id") Long id, PhysicalExaminationRequest request) {
        return physicalExaminationService.update(id, request);
    }

    @DELETE
    @Path("delete-physical-examination/{id}")
    @Transactional
    @Operation(summary = "Delete physical examination")
    public Response delete(@PathParam("id") Long id) {
        return physicalExaminationService.delete(id);
    }

    @GET
    @Path("get-physical-examinations-by-visit/{visitId}")
    @Operation(summary = "List physical examinations for a visit")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PhysicalExaminationDTO.class, type = SchemaType.ARRAY)))
    public Response listByVisit(@PathParam("visitId") Long visitId) {
        List<PhysicalExaminationDTO> rows = physicalExaminationService.listByVisitId(visitId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, rows)).build();
    }

    @GET
    @Path("get-physical-examination/{id}")
    @Operation(summary = "Get physical examination by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PhysicalExaminationDTO.class)))
    public Response getById(@PathParam("id") Long id) {
        return physicalExaminationService.getById(id);
    }
}
