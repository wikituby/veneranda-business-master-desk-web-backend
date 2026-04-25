package org.example.cafeteria.inventory.store.endpoints;


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
import org.example.cafeteria.inventory.store.services.CanteenStoreService;
import org.example.cafeteria.inventory.store.services.payloads.requests.CanteenStoreRequest;
import org.example.cafeteria.inventory.store.services.payloads.responses.CanteenStoreDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.statics.StatusTypes;

@Path("Cafeteria-store-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Cafeteria store Management Module", description = "Cafeteria store Management")

public class CanteenStoreController {

    @Inject
    CanteenStoreService canteenStoreService;

    @POST
    @Path("create-new-store-canteenItem")
    @Transactional
    @Operation(summary = "create-new-canteen-store", description = "create-new-store")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenStoreDTO.class)))
    public Response createNewCanteenStore(CanteenStoreRequest request) {
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label, canteenStoreService.createNewCanteenStore(request))).build();
    }

    @GET
    @Path("/get-all-stores-canteenItem")
    @Transactional
    @Operation(summary = "get all canteen stores", description = "get all canteen stores.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenStoreDTO.class)))
    public Response getAllCanteenStores() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,canteenStoreService.getAllCanteenStores())).build();
    }
}






