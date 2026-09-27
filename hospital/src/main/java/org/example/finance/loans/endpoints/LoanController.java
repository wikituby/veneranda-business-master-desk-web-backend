package org.example.finance.loans.endpoints;

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
import org.example.finance.loans.services.LoanService;
import org.example.finance.loans.services.payloads.requests.LoanRepaymentRequest;
import org.example.finance.loans.services.payloads.requests.LoanRequest;
import org.example.finance.loans.services.payloads.responses.LoanDto;

import java.util.List;

@Path("financial-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "financial Management Module", description = "financial Management")
public class LoanController {

    @Inject
    LoanService loanService;

    @POST
    @Path("create-new-loan")
    @Transactional
    @Operation(summary = "Create loan", description = "Record a new loan")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = LoanDto.class)))
    public Response createLoan(LoanRequest request) {
        return loanService.createLoan(request);
    }

    @GET
    @Transactional
    @Path("/get-all-loans")
    @Operation(summary = "Get all loans", description = "Retrieve all loans")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = LoanDto.class, type = SchemaType.ARRAY)))
    public Response getAllLoans() {
        List<LoanDto> loans = loanService.getAllLoans();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, loans)).build();
    }

    @PUT
    @Path("update-loan/{id}")
    @Transactional
    @Operation(summary = "Update loan", description = "Update loan by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = LoanDto.class)))
    public Response updateLoan(@PathParam("id") Long id, LoanRequest request) {
        return loanService.updateLoan(id, request);
    }

    @POST
    @Path("record-loan-repayment/{id}")
    @Transactional
    @Operation(summary = "Record repayment", description = "Record a loan repayment")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = LoanDto.class)))
    public Response recordRepayment(@PathParam("id") Long id, LoanRepaymentRequest request) {
        return loanService.recordRepayment(id, request);
    }

    @DELETE
    @Path("/delete-loan/{id}")
    @Transactional
    @Operation(summary = "Delete loan", description = "Delete loan by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteLoan(@PathParam("id") Long id) {
        return loanService.deleteLoan(id);
    }
}
