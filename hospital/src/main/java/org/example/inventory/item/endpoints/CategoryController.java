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
import org.example.inventory.item.services.CategoryService;
import org.example.inventory.item.services.payloads.requests.CategoryRequest;
import org.example.inventory.item.services.payloads.requests.ItemCategoryUpdateRequest;
import org.example.inventory.item.services.payloads.responses.basicResponses.CategoryResponse;
import org.example.inventory.item.services.payloads.responses.dtos.CategoryDTO;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")

public class CategoryController {

    @Inject
    CategoryService categoryService;

    @POST
    @Path("/add-new-item-categories")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new categories", description = "add a new categories.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CategoryResponse.class)))
    public Response addShopItem(CategoryRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, categoryService.createCategory(request))).build();
    }

    @PUT
    @Path("/update-item-category/{id}")
    @Transactional
    @Operation(summary = "Update an existing category", description = "Update the name or parent of an existing item category.")
    @APIResponse(description = "Successful",responseCode = "200",content = @Content(schema = @Schema(implementation = CategoryResponse.class))    )
    public Response updateCategory(ItemCategoryUpdateRequest request) {
        return categoryService.updateCategory(request);
    }

    @DELETE
    @Path("/delete-item-category/{id}")
    @Transactional
    @Operation(summary = "Delete an existing category", description = "Delete an existing item category by its ID.")
    @APIResponse(description = "Successful",responseCode = "200",content = @Content(schema = @Schema(implementation = ResponseMessage.class))    )
    public Response deleteCategory(@PathParam("id") Long id) {
        return categoryService.deleteCategory(id);
    }

    @GET
    @Path("/get-all-item-categories")
    @Operation(summary = "Get all item categories", description = "Retrieve a list of all item categories.")
    @APIResponse(description = "Successful",responseCode = "200",content = @Content(schema = @Schema(implementation = CategoryDTO.class))    )
    public Response getAllCategories() {
        //return itemCategoryService.getAllItemCategories();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, categoryService.getAllItemCategories())).build();

    }

    @GET
    @Path("/get-item-category/{id}")
    @Transactional
    @Operation(summary = "Get item category by ID", description = "Retrieve category details by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CategoryDTO.class)))
    public Response getCategoryById(@PathParam("id") Long id) {
        return categoryService.getCategoryById(id);
    }

    @GET
    @Path("/get-highest-parent-item-category/{id}")
    @Transactional
    @Operation(summary = "Get highest parent item category by ID", description = "Retrieve the top-most parent category for the given category ID.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CategoryDTO.class)))
    public Response getHighestParentCategory(@PathParam("id") Long id) {
        CategoryDTO categoryDTO = categoryService.getHighestParentCategory(id);
        if (categoryDTO == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Category not found for ID: " + id))
                    .build();
        }
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, categoryDTO)).build();
    }



}
