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
import org.example.consultations.services.PresentingComplaintService;
import org.example.consultations.services.payloads.requests.PresentingComplaintRequest;
import org.example.consultations.services.payloads.responses.PresentingComplaintDTO;

import java.util.List;

@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Client Management Module - presenting complaints", description = "Structured presenting complaints")
public class PresentingComplaintController {

    @Inject
    PresentingComplaintService presentingComplaintService;

    @POST
    @Path("create-presenting-complaint/{visitId}")
    @Transactional
    @Operation(summary = "Create presenting complaint for a visit consultation")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PresentingComplaintDTO.class)))
    public Response create(@PathParam("visitId") Long visitId, PresentingComplaintRequest request) {
        return presentingComplaintService.createForVisit(visitId, request);
    }

    @PUT
    @Path("update-presenting-complaint/{id}")
    @Transactional
    @Operation(summary = "Update presenting complaint")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PresentingComplaintDTO.class)))
    public Response update(@PathParam("id") Long id, PresentingComplaintRequest request) {
        return presentingComplaintService.update(id, request);
    }

    @DELETE
    @Path("delete-presenting-complaint/{id}")
    @Transactional
    @Operation(summary = "Delete presenting complaint")
    public Response delete(@PathParam("id") Long id) {
        return presentingComplaintService.delete(id);
    }

    @GET
    @Path("get-presenting-complaints-by-visit/{visitId}")
    @Operation(summary = "List presenting complaints for a visit")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PresentingComplaintDTO.class, type = SchemaType.ARRAY)))
    public Response listByVisit(@PathParam("visitId") Long visitId) {
        List<PresentingComplaintDTO> rows = presentingComplaintService.listByVisitId(visitId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, rows)).build();
    }

    @GET
    @Path("get-presenting-complaint/{id}")
    @Operation(summary = "Get presenting complaint by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PresentingComplaintDTO.class)))
    public Response getById(@PathParam("id") Long id) {
        return presentingComplaintService.getById(id);
    }
}
