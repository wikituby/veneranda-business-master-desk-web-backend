package org.example.procedure.procedure.endpoints;

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
import org.example.procedure.procedure.services.ProcedureCategoryService;
import org.example.procedure.procedure.services.payloads.requests.ProcedureCategoryRequest;
import org.example.procedure.procedure.services.payloads.requests.ProcedureCategoryUpdateRequest;
import org.example.procedure.procedure.services.payloads.responses.basicResponses.ProcedureCategoryResponse;
import org.example.procedure.procedure.services.payloads.responses.dtos.ProcedureCategoryDTO;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module - Procedure Categories", description = "Patient Management")

public class ProcedureCategoryController {

    @Inject
    ProcedureCategoryService procedureCategoryService;

    @POST
    @Path("/add-new-procedure-categories")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new procedure categories", description = "add a new procedure categories.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureCategoryResponse.class)))
    public Response addNewProcedureCategories(ProcedureCategoryRequest request) {
        return procedureCategoryService.createCategory(request);
    }

    @PUT
    @Path("/update-procedure-category/{id}")
    @Transactional
    @Operation(summary = "Update an existing procedure category", description = "Update the name or parent of an existing procedure category.")
    @APIResponse(description = "Successful",responseCode = "200",content = @Content(schema = @Schema(implementation = ProcedureCategoryResponse.class))    )
    public Response updateCategory(@PathParam("id") Long id, ProcedureCategoryUpdateRequest request) {
        // Set the categoryId from path parameter to the request
        request.categoryId = id;
        return procedureCategoryService.updateCategory(request);
    }

    @DELETE
    @Path("/delete-procedure-category/{id}")
    @Transactional
    @Operation(summary = "Delete an existing procedure category", description = "Delete an existing procedure category by its ID.")
    @APIResponse(description = "Successful",responseCode = "200",content = @Content(schema = @Schema(implementation = ResponseMessage.class))    )
    public Response deleteCategory(@PathParam("id") Long id) {
        return procedureCategoryService.deleteCategory(id);
    }

    @GET
    @Path("/get-all-procedure-categories")
    @Operation(summary = "Get all procedure categories", description = "Retrieve a list of all procedure categories.")
    @APIResponse(description = "Successful",responseCode = "200",content = @Content(schema = @Schema(implementation = ProcedureCategoryDTO.class))    )
    public Response getAllCategories() {
        //return itemCategoryService.getAllItemCategories();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, procedureCategoryService.getAllItemCategories())).build();

    }

    // Alias for legacy frontend path `get-all-service-categories`
    @GET
    @Path("/get-all-service-categories")
    @Operation(summary = "Get all service categories (alias)", description = "Alias endpoint compatible with existing frontend calls.")
    @APIResponse(description = "Successful",responseCode = "200",content = @Content(schema = @Schema(implementation = ProcedureCategoryDTO.class))    )
    public Response getAllServiceCategories() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, procedureCategoryService.getAllItemCategories())).build();
    }

    @GET
    @Path("/get-procedure-category/{id}")
    @Transactional
    @Operation(summary = "Get procedure category by ID", description = "Retrieve procedure category details by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureCategoryDTO.class)))
    public Response getCategoryById(@PathParam("id") Long id) {
        return procedureCategoryService.getCategoryById(id);
    }

}






