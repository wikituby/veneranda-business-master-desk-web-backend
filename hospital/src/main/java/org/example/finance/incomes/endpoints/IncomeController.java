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
import org.example.finance.incomes.services.IncomeService;
import org.example.finance.incomes.services.payloads.requests.IncomeEntryRequest;
import org.example.finance.incomes.services.payloads.responses.IncomeDto;
import org.example.finance.incomes.services.payloads.responses.IncomeSummaryDto;

import java.util.List;

@Path("financial-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "financial Management Module", description = "financial Management")
public class IncomeController {

    @Inject
    IncomeService incomeService;

    @GET
    @Transactional
    @Path("/get-income-summary")
    @Operation(summary = "Get income summary", description = "Income totals by account with grand total")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = IncomeSummaryDto.class)))
    public Response getIncomeSummary(
            @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo) {
        IncomeSummaryDto summary = incomeService.getIncomeSummary(dateFrom, dateTo);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, summary)).build();
    }

    @GET
    @Transactional
    @Path("/get-all-income-entries")
    @Operation(summary = "Get income entries", description = "List manual income entries")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = IncomeDto.class, type = SchemaType.ARRAY)))
    public Response getAllIncomeEntries(
            @QueryParam("accountType") String accountType,
            @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo) {
        List<IncomeDto> entries = incomeService.getAllIncomeEntries(accountType, dateFrom, dateTo);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, entries)).build();
    }

    @POST
    @Path("create-new-income-entry")
    @Transactional
    @Operation(summary = "Create income entry", description = "Record donation, investor, grant, or other income")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = IncomeDto.class)))
    public Response createIncomeEntry(IncomeEntryRequest request) {
        return incomeService.createIncomeEntry(request);
    }

    @PUT
    @Path("update-income-entry/{id}")
    @Transactional
    @Operation(summary = "Update income entry", description = "Update income entry by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = IncomeDto.class)))
    public Response updateIncomeEntry(@PathParam("id") Long id, IncomeEntryRequest request) {
        return incomeService.updateIncomeEntry(id, request);
    }

    @DELETE
    @Path("/delete-income-entry/{id}")
    @Transactional
    @Operation(summary = "Delete income entry", description = "Delete income entry by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteIncomeEntry(@PathParam("id") Long id) {
        return incomeService.deleteIncomeEntry(id);
    }
}