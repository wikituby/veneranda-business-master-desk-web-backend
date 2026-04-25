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
import org.example.inventory.stock.services.ExpiryItemRegisterService;
import org.example.inventory.stock.services.payloads.requests.ExpiryItemRegisterRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.ExpiryItemRegisterDTO;

@Path("/expiry-item-register")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Register expired or removed stock batches")
public class ExpiryItemRegisterController {

    @Inject
    ExpiryItemRegisterService expiryItemRegisterService;

    @POST
    @Path("/add-new-expiry-item-register")
    @Transactional
    @Operation(summary = "Add expiry / removal register entry", description = "Record an expired or removed batch snapshot")
    @APIResponse(responseCode = "200", description = "Successful",
            content = @Content(schema = @Schema(implementation = ExpiryItemRegisterDTO.class)))
    public Response addNew(ExpiryItemRegisterRequest request) {
        return expiryItemRegisterService.addNew(request);
    }

    @PUT
    @Path("/update-expiry-item-register/{id}")
    @Transactional
    @Operation(summary = "Update register entry", description = "Update an existing expiry item register row")
    @APIResponse(responseCode = "200", description = "Successful",
            content = @Content(schema = @Schema(implementation = ExpiryItemRegisterDTO.class)))
    public Response update(@PathParam("id") Long id, ExpiryItemRegisterRequest request) {
        return expiryItemRegisterService.update(id, request);
    }

    @DELETE
    @Path("/delete-expiry-item-register/{id}")
    @Transactional
    @Operation(summary = "Delete register entry", description = "Delete by primary key")
    @APIResponse(responseCode = "200", description = "Successful")
    public Response delete(@PathParam("id") Long id) {
        return expiryItemRegisterService.delete(id);
    }

    @GET
    @Path("/get-all-expiry-item-registers")
    @Transactional
    @Operation(summary = "List all entries", description = "All expiry / removal register rows, newest first")
    @APIResponse(responseCode = "200", description = "Successful",
            content = @Content(schema = @Schema(implementation = ExpiryItemRegisterDTO.class)))
    public Response getAll() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, expiryItemRegisterService.getAll())).build();
    }

    @GET
    @Path("/get-expiry-item-register/{id}")
    @Transactional
    @Operation(summary = "Get by id", description = "Single register entry")
    @APIResponse(responseCode = "200", description = "Successful",
            content = @Content(schema = @Schema(implementation = ExpiryItemRegisterDTO.class)))
    public Response getById(@PathParam("id") Long id) {
        return expiryItemRegisterService.getById(id);
    }

    @GET
    @Path("/get-expiry-item-registers-by-stock-item/{stockItemId}")
    @Transactional
    @Operation(summary = "List by stock item", description = "All register rows for one stock item")
    @APIResponse(responseCode = "200", description = "Successful",
            content = @Content(schema = @Schema(implementation = ExpiryItemRegisterDTO.class)))
    public Response getByStockItemId(@PathParam("stockItemId") Long stockItemId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,
                expiryItemRegisterService.getByStockItemId(stockItemId))).build();
    }
}
