package org.example.procedure.itemUsedInProcedure.endpoints;

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
import org.example.procedure.itemUsedInProcedure.services.payloads.responses.ItemUsedDTO;
import org.example.procedure.itemUsedInProcedure.services.payloads.requests.ItemUsedRequest;
import org.example.procedure.itemUsedInProcedure.services.ItemUsedService;

import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class ItemUsedController {

    @Inject
    ItemUsedService itemUsedService;

    @POST
    @Path("/add-itemUsed")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new item used to make lab test", description = "add a new item used to make lab test.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemUsedDTO.class)))
    public Response addItemUsedToLabTest(ItemUsedRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label,itemUsedService.addItemUsed(request))).build();
    }

    @GET
    @Path("/items-used/{procedureId}")
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get items used in a procedure", description = "Returns all items used in the given procedure, ordered descending by ID.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemUsedDTO.class)))
    public Response getItemsUsedByProcedure(@PathParam("procedureId") Long procedureId) {
        List<ItemUsedDTO> items = itemUsedService.getItemsUsedByProcedure(procedureId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, items)).build();
    }


    @DELETE
    @Path("/delete-item-used-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete item used by id", description = "delete item used by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteItemUsedById(@PathParam("id") Long id){
        return itemUsedService.deleteItemUsed(id);
    }






}






