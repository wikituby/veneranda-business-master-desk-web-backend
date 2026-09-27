package org.example.finance.incomes.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
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
import org.example.finance.incomes.services.IncomeCategoryService;
import org.example.finance.incomes.services.payloads.requests.IncomeCategoryRequest;
import org.example.finance.incomes.services.payloads.responses.IncomeCategoryDto;
import java.util.List;

@Path("financial-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "financial Management Module", description = "financial Management")
public class IncomeCategoryController {

    @Inject
    IncomeCategoryService incomeCategoryService;

    @POST
    @Path("create-new-income-category")
    @Transactional
    @Operation(summary = "Create income category")
    public Response createIncomeCategory(IncomeCategoryRequest request) {
        return incomeCategoryService.createIncomeCategory(request);
    }

    @GET
    @Transactional
    @Path("/get-all-income-categories")
    @Operation(summary = "Get all income categories")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = IncomeCategoryDto.class, type = SchemaType.ARRAY)))
    public Response getAllIncomeCategories() {
        List<IncomeCategoryDto> list = incomeCategoryService.getAllIncomeCategories();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, list)).build();
    }

    @PUT
    @Path("update-income-category/{id}")
    @Transactional
    public Response updateIncomeCategory(@PathParam("id") Long id, IncomeCategoryRequest request) {
        return incomeCategoryService.updateIncomeCategory(id, request);
    }

    @DELETE
    @Path("delete-income-category/{id}")
    @Transactional
    public Response deleteIncomeCategory(@PathParam("id") Long id) {
        return incomeCategoryService.deleteIncomeCategory(id);
    }
}
