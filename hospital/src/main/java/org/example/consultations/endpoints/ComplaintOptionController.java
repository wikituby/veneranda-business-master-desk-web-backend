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
import jakarta.ws.rs.QueryParam;
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
import org.example.consultations.services.ComplaintOptionService;
import org.example.consultations.services.payloads.requests.ComplaintOptionRequest;
import org.example.consultations.services.payloads.responses.ComplaintOptionDTO;

import java.util.List;

@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Client Management Module - Complaint Options", description = "Reusable presenting-complaint option catalogs")
public class ComplaintOptionController {

    @Inject
    ComplaintOptionService complaintOptionService;

    @POST
    @Path("/create-new-complaint-option")
    @Transactional
    @Operation(summary = "Create a complaint option")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintOptionDTO.class)))
    public Response create(ComplaintOptionRequest request) {
        return complaintOptionService.create(request);
    }

    @PUT
    @Path("/update-complaint-option/{id}")
    @Transactional
    @Operation(summary = "Update a complaint option")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintOptionDTO.class)))
    public Response update(@PathParam("id") Long id, ComplaintOptionRequest request) {
        return complaintOptionService.update(id, request);
    }

    @DELETE
    @Path("/delete-complaint-option/{id}")
    @Transactional
    @Operation(summary = "Delete a complaint option")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ResponseMessage.class)))
    public Response delete(@PathParam("id") Long id) {
        return complaintOptionService.delete(id);
    }

    @GET
    @Path("/get-all-complaint-options")
    @Transactional
    @Operation(summary = "Get all complaint options (optionally filter by category)")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintOptionDTO.class, type = SchemaType.ARRAY)))
    public Response getAll(@QueryParam("category") String category) {
        List<ComplaintOptionDTO> rows = (category != null && !category.isBlank())
                ? complaintOptionService.listByCategory(category)
                : complaintOptionService.listAll();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, rows)).build();
    }

    @GET
    @Path("/get-complaint-option/{id}")
    @Transactional
    @Operation(summary = "Get complaint option by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintOptionDTO.class)))
    public Response getById(@PathParam("id") Long id) {
        return complaintOptionService.getById(id);
    }
}
