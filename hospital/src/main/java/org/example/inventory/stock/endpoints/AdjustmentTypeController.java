package org.example.inventory.stock.endpoints;

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
import org.example.inventory.stock.services.AdjustmentTypeService;
import org.example.inventory.stock.services.payloads.requests.AdjustmentTypeRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.AdjustmentTypeDTO;

@Path("/adjustment-type")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Stock adjustment type catalog")
public class AdjustmentTypeController {

    @Inject
    AdjustmentTypeService adjustmentTypeService;

    @POST
    @Path("/add-new-adjustment-type")
    @Transactional
    @Operation(summary = "Create adjustment type")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AdjustmentTypeDTO.class)))
    public Response addNew(AdjustmentTypeRequest request) {
        return adjustmentTypeService.addNew(request);
    }

    @PUT
    @Path("/update-adjustment-type/{id}")
    @Transactional
    @Operation(summary = "Update adjustment type")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AdjustmentTypeDTO.class)))
    public Response update(@PathParam("id") Long id, AdjustmentTypeRequest request) {
        return adjustmentTypeService.update(id, request);
    }

    @DELETE
    @Path("/delete-adjustment-type/{id}")
    @Transactional
    @Operation(summary = "Delete adjustment type (only if unused)")
    @APIResponse(responseCode = "200")
    public Response delete(@PathParam("id") Long id) {
        return adjustmentTypeService.delete(id);
    }

    @GET
    @Path("/get-all-adjustment-types")
    @Transactional
    @Operation(summary = "List all adjustment types")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AdjustmentTypeDTO.class)))
    public Response getAll() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, adjustmentTypeService.getAll())).build();
    }

    @GET
    @Path("/get-active-adjustment-types")
    @Transactional
    @Operation(summary = "List active adjustment types only")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AdjustmentTypeDTO.class)))
    public Response getActive() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, adjustmentTypeService.getAllActive())).build();
    }

    @GET
    @Path("/get-adjustment-type/{id}")
    @Transactional
    @Operation(summary = "Get adjustment type by id")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AdjustmentTypeDTO.class)))
    public Response getById(@PathParam("id") Long id) {
        return adjustmentTypeService.getById(id);
    }
}
