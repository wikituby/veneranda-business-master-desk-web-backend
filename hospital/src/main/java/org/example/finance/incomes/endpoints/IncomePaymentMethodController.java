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
import org.example.finance.incomes.services.IncomePaymentMethodService;
import org.example.finance.incomes.services.payloads.requests.IncomePaymentMethodRequest;
import org.example.finance.incomes.services.payloads.responses.IncomePaymentMethodDto;
import java.util.List;

@Path("financial-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "financial Management Module", description = "financial Management")
public class IncomePaymentMethodController {

    @Inject
    IncomePaymentMethodService incomePaymentMethodService;

    @POST
    @Path("create-new-income-payment-method")
    @Transactional
    public Response createIncomePaymentMethod(IncomePaymentMethodRequest request) {
        return incomePaymentMethodService.createIncomePaymentMethod(request);
    }

    @GET
    @Transactional
    @Path("/get-all-income-payment-methods")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = IncomePaymentMethodDto.class, type = SchemaType.ARRAY)))
    public Response getAllIncomePaymentMethods() {
        List<IncomePaymentMethodDto> list = incomePaymentMethodService.getAllIncomePaymentMethods();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, list)).build();
    }

    @PUT
    @Path("update-income-payment-method/{id}")
    @Transactional
    public Response updateIncomePaymentMethod(@PathParam("id") Long id, IncomePaymentMethodRequest request) {
        return incomePaymentMethodService.updateIncomePaymentMethod(id, request);
    }

    @DELETE
    @Path("delete-income-payment-method/{id}")
    @Transactional
    public Response deleteIncomePaymentMethod(@PathParam("id") Long id) {
        return incomePaymentMethodService.deleteIncomePaymentMethod(id);
    }
}
