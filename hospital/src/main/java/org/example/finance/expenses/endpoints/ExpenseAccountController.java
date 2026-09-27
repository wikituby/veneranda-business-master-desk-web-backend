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
import org.example.finance.expenses.services.ExpenseAccountService;
import org.example.finance.expenses.services.payloads.requests.ExpenseAccountRequest;
import org.example.finance.expenses.services.payloads.responses.ExpenseAccountDto;
import org.example.finance.expenses.services.payloads.responses.ExpenseTransactionDto;

import java.util.List;

@Path("financial-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "financial Management Module", description = "financial Management")

public class ExpenseAccountController {

    @Inject
    ExpenseAccountService expenseAccountService;

    @POST
    @Path("create-new-expense-account")
    @Transactional
    @Operation(summary = "new expense account", description = "new expense account")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ExpenseAccountDto.class)))
    public Response createNewExpenseAccount(ExpenseAccountRequest request){
        return expenseAccountService.createExpenseAccount(request);
    }

    @GET
    @Transactional
    @Path("/get-all-expense-accounts")
    @Operation(summary = "Get all expense accounts", description = "Retrieve a list of all expense accounts")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ExpenseAccountDto.class, type = SchemaType.ARRAY)))
    public Response getAllPatients() {
        List<ExpenseAccountDto> expenseAccountDto = expenseAccountService.getAllExpenseAccount();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, expenseAccountDto)).build();
    }

    @PUT
    @Path("update-expense-account/{id}")
    @Transactional
    @Operation(summary = "Update expense account", description = "Update an existing expense account")
    public Response updateExpenseAccount(@PathParam("id") Long id, ExpenseAccountRequest request) {
        return expenseAccountService.updateExpenseAccount(id, request);
    }

    @DELETE
    @Path("delete-expense-account/{id}")
    @Transactional
    @Operation(summary = "Delete expense account", description = "Delete an expense account by ID")
    public Response deleteExpenseAccount(@PathParam("id") Long id) {
        return expenseAccountService.deleteExpenseAccount(id);
    }
}






