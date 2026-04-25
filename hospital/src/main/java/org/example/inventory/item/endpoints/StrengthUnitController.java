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
import org.example.inventory.item.services.StrengthUnitService;
import org.example.inventory.item.services.payloads.requests.StrengthUnitRequest;
import org.example.inventory.item.services.payloads.responses.basicResponses.StrengthUnitResponse;
import org.example.inventory.item.services.payloads.responses.dtos.StrengthUnitDTO;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")

public class StrengthUnitController {

    @Inject
    StrengthUnitService strengthUnitService;

    @POST
    @Path("/add-new-strength-unit")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new strength unit", description = "add a new strength unit.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StrengthUnitResponse.class)))
    public Response addNewStrengthUnit(StrengthUnitRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, strengthUnitService.addNewStrengthUnit(request))).build();
    }

    @GET
    @Path("/get-all-strength-units")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all strength units", description = "Retrieve a list of all strength units")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StrengthUnitDTO.class, type = SchemaType.ARRAY)))
    public Response getAllStrengthUnits() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, strengthUnitService.getAllStrengthUnits())).build();
    }

    @GET
    @Path("/get-strength-unit/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get strength unit by ID", description = "Retrieve strength unit details by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StrengthUnitDTO.class)))
    public Response getStrengthUnitById(@PathParam("id") Long id) {
        return strengthUnitService.getStrengthUnitById(id);
    }

    @PUT
    @Path("/update-strength-unit/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Update strength unit", description = "Update an existing strength unit by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StrengthUnitDTO.class)))
    public Response updateStrengthUnit(@PathParam("id") Long id, StrengthUnitRequest request) {
        return strengthUnitService.updateStrengthUnit(id, request);
    }

    @DELETE
    @Path("/delete-strength-unit/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Delete strength unit", description = "Delete a strength unit by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteStrengthUnit(@PathParam("id") Long id) {
        return strengthUnitService.deleteStrengthUnit(id);
    }
}
