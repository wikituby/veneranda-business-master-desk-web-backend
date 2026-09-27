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
import org.example.finance.incomes.services.IncomeAccountService;
import org.example.finance.incomes.services.payloads.requests.IncomeAccountRequest;
import org.example.finance.incomes.services.payloads.responses.IncomeAccountDto;
import java.util.List;

@Path("financial-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "financial Management Module", description = "financial Management")
public class IncomeAccountController {

    @Inject
    IncomeAccountService incomeAccountService;

    @POST
    @Path("create-new-income-account")
    @Transactional
    @Operation(summary = "Create income account")
    public Response createIncomeAccount(IncomeAccountRequest request) {
        return incomeAccountService.createIncomeAccount(request);
    }

    @GET
    @Transactional
    @Path("/get-all-income-accounts")
    @Operation(summary = "Get all income accounts")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = IncomeAccountDto.class, type = SchemaType.ARRAY)))
    public Response getAllIncomeAccounts() {
        List<IncomeAccountDto> list = incomeAccountService.getAllIncomeAccounts();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, list)).build();
    }

    @PUT
    @Path("update-income-account/{id}")
    @Transactional
    public Response updateIncomeAccount(@PathParam("id") Long id, IncomeAccountRequest request) {
        return incomeAccountService.updateIncomeAccount(id, request);
    }

    @DELETE
    @Path("delete-income-account/{id}")
    @Transactional
    public Response deleteIncomeAccount(@PathParam("id") Long id) {
        return incomeAccountService.deleteIncomeAccount(id);
    }
}
