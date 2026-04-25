package org.example.inventory.item.endpoints;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.services.ActiveIngredientService;
import org.example.inventory.item.services.payloads.requests.ActiveIngredientRequest;
import org.example.inventory.item.services.payloads.requests.ActiveIngredientUpdateRequest;
import org.example.inventory.item.services.payloads.responses.basicResponses.ActiveIngredientResponse;
import org.example.inventory.item.services.payloads.responses.dtos.ActiveIngredientDTO;

import java.util.List;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")
public class ActiveIngredientController {

    @Inject
    ActiveIngredientService activeIngredientService;

    @POST
    @Path("/add-new-active-ingredients")
    @Transactional
    @Operation(summary = "add a new active ingredient", description = "add a new active ingredient.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ActiveIngredientResponse.class)))
    public Response addActiveIngredient(ActiveIngredientRequest request) {
        return activeIngredientService.addActiveIngredient(request);
    }

    @POST
    @Path("/add-new-bulk-active-ingredients")
    @Transactional
    @Operation(summary = "Add multiple active ingredients", description = "Adds a list of new active ingredients.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ActiveIngredientResponse.class)))
    public Response addActiveIngredients(List<ActiveIngredientRequest> requests) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, activeIngredientService.addActiveIngredients(requests))).build();
    }

    @GET
    @Path("/get-all-active-ingredients")
    @Transactional
    @Operation(summary = "get all active ingredients", description = "get all active ingredients.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ActiveIngredientDTO.class)))
    public Response getActiveIngredients() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, activeIngredientService.listLatestFirst())).build();
    }

    @GET
    @Path("/get-active-ingredient/{id}")
    @Transactional
    @Operation(summary = "get active ingredient by id", description = "get active ingredient by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ActiveIngredientResponse.class)))
    public Response getActiveIngredientById(@PathParam("id") Long id) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, activeIngredientService.getActiveIngredientById(id))).build();
    }

    @DELETE
    @Path("/delete-all-active-ingredients")
    @Transactional
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "delete all active ingredients", description = "delete all active ingredients.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteAllActiveIngredients() {
        activeIngredientService.deleteAllActiveIngredients();
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }

    @PUT
    @Path("/update-active-ingredient/{id}")
    @Transactional
    @Operation(summary = "Update active ingredient by Id", description = "Update active ingredient by Id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ActiveIngredientDTO.class)))
    public Response updateActiveIngredient(@PathParam("id") Long id, ActiveIngredientUpdateRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, activeIngredientService.updateActiveIngredientById(id, request))).build();
    }

    @DELETE
    @Path("/delete-active-ingredient/{id}")
    @Transactional
    @Operation(summary = "delete active ingredient by id ", description = "delete active ingredient by id.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteActiveIngredientById(@PathParam("id") Long id) {
        return activeIngredientService.deleteActiveIngredientById(id);
    }

}
