package org.example.store.endpoints;

import jakarta.annotation.security.PermitAll;
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
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.store.services.StoreService;
import org.example.store.services.payloads.requests.StoreRequest;
import org.example.store.services.payloads.requests.StoreUpdateRequest;
import org.example.store.services.payloads.responses.StoreDTO;
import org.example.store.services.payloads.responses.basicResponses.StoreResponse;

@Path("/stores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Stores", description = "Store management")
@PermitAll

public class StoreController {

    @Inject
    StoreService storeService;

    @POST
    @Path("/add-new-store")
    @Transactional
    @Operation(summary = "Create a new store", description = "Create a new store.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StoreResponse.class)))
    public Response createNewStore(StoreRequest request) {
        return storeService.createNewStore(request);
    }

    @PUT
    @Path("/update-store/{id}")
    @Transactional
    @Operation(summary = "Update an existing store", description = "Update store name, location or description.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StoreResponse.class)))
    public Response updateStore(@PathParam("id") Long id, StoreUpdateRequest request) {
        request.storeId = id;
        return storeService.updateStore(request);
    }

    @DELETE
    @Path("/delete-store/{id}")
    @Transactional
    @Operation(summary = "Delete a store", description = "Delete a store by ID.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ResponseMessage.class)))
    public Response deleteStore(@PathParam("id") Long id) {
        return storeService.deleteStore(id);
    }

    @GET
    @Path("/get-all-stores")
    @Operation(summary = "Get all stores", description = "Retrieve a list of all stores.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StoreDTO.class)))
    public Response getAllStores() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, storeService.getAllStores())).build();
    }

    @GET
    @Path("/get-store/{id}")
    @Transactional
    @Operation(summary = "Get store by ID", description = "Retrieve store details by ID.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StoreDTO.class)))
    public Response getStoreById(@PathParam("id") Long id) {
        return storeService.getStoreById(id);
    }
}
