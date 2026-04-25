package org.example.inventory.item.endpoints;

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
import org.example.inventory.item.services.StrengthService;
import org.example.inventory.item.services.payloads.requests.StrengthRequest;
import org.example.inventory.item.services.payloads.responses.basicResponses.StrengthResponse;
import org.example.inventory.item.services.payloads.responses.dtos.StrengthDTO;


@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")

public class StrengthController {

    @Inject
    StrengthService strengthService;

    @POST
    @Path("/add-new-strength")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new strength", description = "add a new strength.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StrengthResponse.class)))
    public Response addNewStrength(StrengthRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, strengthService.addNewStrength(request))).build();
    }

    @GET
    @Path("/get-all-strengths")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all strengths", description = "Retrieve a list of all strengths")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StrengthDTO.class, type = SchemaType.ARRAY)))
    public Response getAllStrengths() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, strengthService.getAllStrengths())).build();
    }

    @GET
    @Path("/get-strength/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get strength by ID", description = "Retrieve strength details by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StrengthDTO.class)))
    public Response getStrengthById(@PathParam("id") Long id) {
        return strengthService.getStrengthById(id);
    }

    @PUT
    @Path("/update-strength/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Update strength", description = "Update an existing strength by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StrengthDTO.class)))
    public Response updateStrength(@PathParam("id") Long id, StrengthRequest request) {
        return strengthService.updateStrength(id, request);
    }

    @DELETE
    @Path("/delete-strength/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Delete strength", description = "Delete a strength by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteStrength(@PathParam("id") Long id) {
        return strengthService.deleteStrength(id);
    }
}
