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
import org.example.finance.expenses.services.ExpenseTransactionService;
import org.example.finance.expenses.services.payloads.requests.ExpenseTransactionRequest;
import org.example.finance.expenses.services.payloads.responses.ExpenseTransactionDto;
import org.example.visit.services.paloads.responses.PatientVisitDTO;

import java.util.List;

@Path("financial-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "financial Management Module", description = "financial Management")

public class ExpenseTransactionController {

    @Inject
    ExpenseTransactionService expenseTransactionService;

    @POST
    @Path("create-new-expense-transaction")
    @Operation(summary = "new expense transaction", description = "new expense transaction")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ExpenseTransactionDto.class)))
    public Response createNewExpenseTransaction(ExpenseTransactionRequest request){
        return expenseTransactionService.createExpenseTransaction(request);
    }

    @GET
    @Transactional
    @Path("/get-all-expense-transactions")
    @Operation(summary = "Get all expense transactions", description = "Retrieve a list of all expense transactions")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ExpenseTransactionDto.class, type = SchemaType.ARRAY)))
    public Response getAllPatients() {
        List<ExpenseTransactionDto> expenseTransactionDto = expenseTransactionService.getAllExpenseTransactions();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, expenseTransactionDto)).build();
    }

    @DELETE
    @Path("/delete-expense-transaction/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete expense transaction by id ", description = "delete expense Transaction by id.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteExpenseTransactionById(@PathParam("id") Long id){
        return expenseTransactionService.deleteExpenseTransactionById(id);

    }

    @PUT
    @Path("update-expense-transaction/{id}")
    @Transactional
    @Operation(summary = "update expense transaction by id", description = "update expense Transaction by id.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ExpenseTransactionDto.class)))
    public Response updateExpenseTransactionById(@PathParam("id") Long id, ExpenseTransactionRequest request){
        return expenseTransactionService.updateExpenseTransactionById(id, request);
    }
}






