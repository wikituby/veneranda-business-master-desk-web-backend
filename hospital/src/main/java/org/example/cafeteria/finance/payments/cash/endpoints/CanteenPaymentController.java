package org.example.cafeteria.finance.payments.cash.endpoints;

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
import org.example.cafeteria.finance.payments.cash.services.CanteenPaymentService;
import org.example.cafeteria.finance.payments.cash.services.payloads.requests.CanteenPaymentParametersRequest;
import org.example.cafeteria.finance.payments.cash.services.payloads.requests.CanteenPaymentRequest;
import org.example.cafeteria.finance.payments.cash.services.payloads.responses.CanteenPaymentDTO;
import org.example.cafeteria.sales.saleDay.services.payloads.responses.SaleDayDTO;
import org.example.cafeteria.sales.saleDone.domains.Sale;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

import java.util.List;


@Path("cafeteria-payment-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "cafeteria payment Management Module", description = "cafeteria payment Management")

public class CanteenPaymentController {

    @Inject
    CanteenPaymentService paymentsService;


    @POST
    @Path("create-new-buyer-payment/{id}")
    @Transactional
    @Operation(summary = "new-payment", description = "new-payment")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenPaymentDTO.class)))
    public Response createNewPayment(@PathParam("id") Long visitId, CanteenPaymentRequest request) {
        return paymentsService.createNewPayment(visitId,request);
    }

    @GET
    @Path("get-buyer-payment-List-by-visit-id/{id}")
    @Operation(summary = "Get payment List where visit id", description = "Get payment List where visit id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SaleDayDTO.class)))
    public Response getPaymentListByVisitId(@PathParam("id") Long visitId) {
        // Call the service method to get a list of InitialTriageVitalsDTO for the given visitId
        List<CanteenPaymentDTO> paymentList = paymentsService.getPaymentsByVisitId(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, paymentList)).build();
    }

    @DELETE
    @Path("/delete-buyer-payment-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete payment by id", description = "delete payment by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deletePaymentById(@PathParam("id") Long id){
        return paymentsService.deletePayment(id);
    }


    @GET
    @Path("/get-buyer-payment-advanced-search")
    //@RolesAllowed({"ADMIN","USER","AGENT"})
    @Operation(summary = "get payment advanced search", description = "get payment advanced search.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Sale.class)))
    public Response getPaymentAdvancedFilter(@BeanParam CanteenPaymentParametersRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,paymentsService.getPaymentsAdvancedFilter(request))).build();
    }

}






