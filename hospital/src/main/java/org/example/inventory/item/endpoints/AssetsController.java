package org.example.inventory.item.endpoints;

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
import org.example.inventory.item.domain.Assets;
import org.example.inventory.item.services.AssetsService;
import org.example.inventory.item.services.payloads.requests.AssetsRequest;

import java.util.List;

@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Inventory Management - Assets", description = "Endpoints for managing hospital assets and equipment.")
public class AssetsController {

    @Inject
    AssetsService assetsService;

    // ✅ CREATE
    @POST
    @Path("/add")
    @Transactional
    @Operation(summary = "Add new asset", description = "Registers a new hospital asset.")
    @APIResponse(description = "Asset successfully added", responseCode = "200",
            content = @Content(schema = @Schema(implementation = ResponseMessage.class)))
    public Response addAsset(AssetsRequest request) {
        return Response.ok(assetsService.addAsset(request)).build();
    }

    // ✅ GET ALL
    @GET
    @Path("/all")
    @Operation(summary = "Get all assets", description = "Retrieves all registered hospital assets.")
    @APIResponse(description = "List of assets", responseCode = "200",
            content = @Content(schema = @Schema(implementation = Assets.class)))
    public Response getAllAssets() {
        List<Assets> assets = assetsService.getAllAssets();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, assets)).build();
    }

    // ✅ GET BY ID
    @GET
    @Path("/{id}")
    @Operation(summary = "Get asset by ID", description = "Fetches a specific asset using its ID.")
    @APIResponse(description = "Asset details", responseCode = "200",
            content = @Content(schema = @Schema(implementation = Assets.class)))
    public Response getAssetById(@PathParam("id") Long id) {
        Assets asset = assetsService.getAssetById(id);
        if (asset == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Asset not found"))
                    .build();
        }
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, asset)).build();
    }

    // ✅ UPDATE
    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update asset", description = "Updates details of an existing asset by ID.")
    @APIResponse(description = "Asset successfully updated", responseCode = "200",
            content = @Content(schema = @Schema(implementation = ResponseMessage.class)))
    public Response updateAsset(@PathParam("id") Long id, AssetsRequest request) {
        return Response.ok(assetsService.updateAsset(id, request)).build();
    }

    // ✅ DELETE
    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete asset", description = "Removes a hospital asset by ID.")
    @APIResponse(description = "Asset deleted successfully", responseCode = "200",
            content = @Content(schema = @Schema(implementation = ResponseMessage.class)))
    public Response deleteAsset(@PathParam("id") Long id) {
        return Response.ok(assetsService.deleteAsset(id)).build();
    }
}
