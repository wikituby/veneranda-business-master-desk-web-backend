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
import org.example.consultations.services.ComplaintSiteService;
import org.example.consultations.services.payloads.requests.ComplaintSiteRequest;
import org.example.consultations.services.payloads.responses.ComplaintSiteDTO;

import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module - Complaint Sites", description = "Patient Management")
public class ComplaintSiteController {

    @Inject
    ComplaintSiteService complaintSiteService;

    @POST
    @Path("/create-new-site")
    @Transactional
    @Operation(summary = "Create a new complaint site", description = "Creates a new complaint site with title and description")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintSiteDTO.class)))
    public Response createComplaintSite(ComplaintSiteRequest request) {
        return complaintSiteService.createComplaintSite(request);
    }

    @PUT
    @Path("/update-complaint-site/{id}")
    @Transactional
    @Operation(summary = "Update an existing complaint site", description = "Updates the title and description of an existing complaint site")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintSiteDTO.class)))
    public Response updateComplaintSite(@PathParam("id") Long id, ComplaintSiteRequest request) {
        return complaintSiteService.updateComplaintSite(id, request);
    }

    @DELETE
    @Path("/delete-complaint-site/{id}")
    @Transactional
    @Operation(summary = "Delete an existing complaint site", description = "Deletes an existing complaint site by its ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ResponseMessage.class)))
    public Response deleteComplaintSite(@PathParam("id") Long id) {
        return complaintSiteService.deleteComplaintSite(id);
    }

    @GET
    @Path("/get-all-sites")
    @Transactional
    @Operation(summary = "Get all complaint sites", description = "Retrieves a list of all complaint sites")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintSiteDTO.class, type = SchemaType.ARRAY)))
    public Response getAllComplaintSites() {
        List<ComplaintSiteDTO> complaintSites = complaintSiteService.getAllComplaintSites();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, complaintSites)).build();
    }

    @GET
    @Path("/get-complaint-site/{id}")
    @Transactional
    @Operation(summary = "Get complaint site by ID", description = "Retrieves a complaint site by its ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ComplaintSiteDTO.class)))
    public Response getComplaintSiteById(@PathParam("id") Long id) {
        return complaintSiteService.getComplaintSiteById(id);
    }
}






