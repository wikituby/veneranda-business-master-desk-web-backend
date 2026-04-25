package org.example.finance.expenses.endpoints;

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
import org.example.finance.expenses.services.ExpenseCategoryService;
import org.example.finance.expenses.services.payloads.requests.ExpenseCategoryRequest;
import org.example.finance.expenses.services.payloads.responses.ExpenseCategoryDto;
import org.example.finance.expenses.services.payloads.responses.ExpenseTransactionDto;

import java.util.List;

@Path("financial-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "financial Management Module", description = "financial Management")

public class ExpenseCategoryController {

    @Inject
    ExpenseCategoryService expenseCategoryService;

    @POST
    @Path("create-new-expense-category")
    @Transactional
    @Operation(summary = "new expense category", description = "new expense category")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ExpenseCategoryDto.class)))
    public Response createNewExpenseCategory(ExpenseCategoryRequest request){
        return expenseCategoryService.createExpenseCategory(request);
    }

    @GET
    @Transactional
    @Path("/get-all-expense-categories")
    @Operation(summary = "Get all expense categories", description = "Retrieve a list of all expense categories")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ExpenseCategoryDto.class, type = SchemaType.ARRAY)))
    public Response getAllExpenseCategories() {
        List<ExpenseCategoryDto> expenseCategoryDto = expenseCategoryService.getAllExpenseCategory();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, expenseCategoryDto)).build();
    }
}






