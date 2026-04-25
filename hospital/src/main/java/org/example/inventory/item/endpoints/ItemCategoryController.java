package org.example.inventory.item.endpoints;

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
import org.example.inventory.item.services.ItemCategoryService;
import org.example.inventory.item.services.payloads.requests.ItemCategoryRequest;
import org.example.inventory.item.services.payloads.requests.ItemCategoryUpdateRequest;
import org.example.inventory.item.services.payloads.responses.basicResponses.ItemCategoryResponse;
import org.example.inventory.item.services.payloads.responses.dtos.ItemCategoryDTO;

@Path("/item-categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Item Categories", description = "Item category management")
@PermitAll

public class ItemCategoryController {

    @Inject
    ItemCategoryService itemCategoryService;

    @POST
    @Path("/add-new-item-category")
    @Transactional
    @Operation(summary = "Add a new item category", description = "Add a new item category.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemCategoryResponse.class)))
    public Response addNewItemCategory(ItemCategoryRequest request) {
        return itemCategoryService.createCategory(request);
    }

    @PUT
    @Path("/update-item-category/{id}")
    @Transactional
    @Operation(summary = "Update an existing item category", description = "Update the name or parent of an existing item category.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemCategoryResponse.class)))
    public Response updateCategory(@PathParam("id") Long id, ItemCategoryUpdateRequest request) {
        request.categoryId = id;
        return itemCategoryService.updateCategory(request);
    }

    @DELETE
    @Path("/delete-item-category/{id}")
    @Transactional
    @Operation(summary = "Delete an existing item category", description = "Delete an existing item category by its ID.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ResponseMessage.class)))
    public Response deleteCategory(@PathParam("id") Long id) {
        return itemCategoryService.deleteCategory(id);
    }

    @GET
    @Path("/get-all-item-categories")
    @Operation(summary = "Get all item categories", description = "Retrieve a list of all item categories.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemCategoryDTO.class)))
    public Response getAllCategories() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, itemCategoryService.getAllItemCategories())).build();
    }

    @GET
    @Path("/get-item-category/{id}")
    @Transactional
    @Operation(summary = "Get item category by ID", description = "Retrieve item category details by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemCategoryDTO.class)))
    public Response getCategoryById(@PathParam("id") Long id) {
        return itemCategoryService.getCategoryById(id);
    }
}
