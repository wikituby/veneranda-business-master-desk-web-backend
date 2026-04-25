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
import org.example.inventory.item.services.CompositionService;
import org.example.inventory.item.services.payloads.requests.CompositionRequest;
import org.example.inventory.item.services.payloads.responses.basicResponses.CompositionResponse;
import org.example.inventory.item.services.payloads.responses.dtos.CompositionDTO;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")

public class CompositionController {

    @Inject
    CompositionService compositionService;

    @POST
    @Path("/add-new-composition")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new composition", description = "add a new composition.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CompositionResponse.class)))
    public Response addComposition(CompositionRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, compositionService.createComposition(request))).build();
    }

    @GET
    @Path("/get-all-compositions")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all compositions", description = "Retrieve a list of all compositions")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CompositionDTO.class, type = SchemaType.ARRAY)))
    public Response getAllCompositions() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, compositionService.getAllCompositions())).build();
    }

    @GET
    @Path("/get-compositions-by-stock-item/{stockItemId}")
    @Transactional
    @Operation(summary = "Get compositions by stock item ID", description = "Retrieve all compositions for a specific stock item")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CompositionDTO.class, type = SchemaType.ARRAY)))
    public Response getCompositionsByStockItemId(@PathParam("stockItemId") Long stockItemId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, compositionService.getCompositionsByVariantId(stockItemId).stream()
                .map(CompositionDTO::new)
                .collect(java.util.stream.Collectors.toList()))).build();
    }

    @GET
    @Path("/get-composition/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get composition by ID", description = "Retrieve composition details by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CompositionDTO.class)))
    public Response getCompositionById(@PathParam("id") Long id) {
        return compositionService.getCompositionById(id);
    }

    @PUT
    @Path("/update-composition/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Update composition", description = "Update an existing composition by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CompositionDTO.class)))
    public Response updateComposition(@PathParam("id") Long id, CompositionRequest request) {
        return compositionService.updateComposition(id, request);
    }

    @DELETE
    @Path("/delete-composition/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Delete composition", description = "Delete a composition by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteComposition(@PathParam("id") Long id) {
        return compositionService.deleteComposition(id);
    }
}
