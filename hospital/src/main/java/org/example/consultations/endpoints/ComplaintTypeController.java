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
import org.example.consultations.services.ComplaintTypeService;
import org.example.consultations.services.payloads.requests.ComplaintTypeRequest;
import org.example.consultations.services.payloads.responses.ComplaintTypeDTO;

import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module - Complaint Types", description = "Patient Management")
public class ComplaintTypeController {

    @Inject
    ComplaintTypeService complaintTypeService;

    @POST
    @Path("/create-new-complaint-type")
    @Transactional
    @Operation(summary = "Create a new complaint type", description = "Creates a new complaint type with title and description")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintTypeDTO.class)))
    public Response createComplaintType(ComplaintTypeRequest request) {
        return complaintTypeService.createComplaintType(request);
    }

    @PUT
    @Path("/update-complaint-type/{id}")
    @Transactional
    @Operation(summary = "Update an existing complaint type", description = "Updates the title and description of an existing complaint type")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintTypeDTO.class)))
    public Response updateComplaintType(@PathParam("id") Long id, ComplaintTypeRequest request) {
        return complaintTypeService.updateComplaintType(id, request);
    }

    @DELETE
    @Path("/delete-complaint-type/{id}")
    @Transactional
    @Operation(summary = "Delete an existing complaint type", description = "Deletes an existing complaint type by its ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ResponseMessage.class)))
    public Response deleteComplaintType(@PathParam("id") Long id) {
        return complaintTypeService.deleteComplaintType(id);
    }

    @GET
    @Path("/get-all-complaint-types")
    @Transactional
    @Operation(summary = "Get all complaint types", description = "Retrieves a list of all complaint types")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintTypeDTO.class, type = SchemaType.ARRAY)))
    public Response getAllComplaintTypes() {
        List<ComplaintTypeDTO> complaintTypes = complaintTypeService.getAllComplaintTypes();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, complaintTypes)).build();
    }

    @GET
    @Path("/get-complaint-type/{id}")
    @Transactional
    @Operation(summary = "Get complaint type by ID", description = "Retrieves a complaint type by its ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintTypeDTO.class)))
    public Response getComplaintTypeById(@PathParam("id") Long id) {
        return complaintTypeService.getComplaintTypeById(id);
    }
}

















