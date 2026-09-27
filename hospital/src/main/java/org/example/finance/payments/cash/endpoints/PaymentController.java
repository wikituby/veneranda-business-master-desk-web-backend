package org.example.finance.payments.cash.endpoints;

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
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.payments.cash.services.payloads.responses.PaymentDTO;
import org.example.finance.payments.cash.services.payloads.requests.PaymentParametersRequest;
import org.example.finance.payments.cash.services.payloads.requests.PaymentRequest;
import org.example.finance.payments.cash.services.PaymentService;
import org.example.visit.domains.PatientVisit;
import org.example.visit.services.paloads.responses.PatientVisitDTO;

import java.util.List;


@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Client Management Module", description = "Client Management")

public class PaymentController {

    @Inject
    PaymentService paymentsService;


    @POST
    @Path("create-new-payment/{id}")
    @Transactional
    @Operation(summary = "new-payment", description = "new-payment")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PaymentDTO.class)))
    public Response createNewPayment(@PathParam("id") Long visitId, PaymentRequest request) {
        return paymentsService.createNewPayment(visitId,request);
    }

    @POST
    @Path("pay-all-patient-debt/{patientId}")
    @Transactional
    @Operation(summary = "Pay all patient debt", description = "Apply a lump-sum payment across patient visits, oldest first")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PaymentDTO.class)))
    public Response payAllPatientDebt(@PathParam("patientId") Long patientId, PaymentRequest request) {
        return paymentsService.payAllPatientDebt(patientId, request);
    }

    @GET
    @Path("get-payment-List-by-visit-id/{id}")
    @Operation(summary = "Get payment List where visit id", description = "Get payment List where visit id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientVisitDTO.class)))
    public Response getPaymentListByVisitId(@PathParam("id") Long visitId) {
        // Call the service method to get a list of InitialTriageVitalsDTO for the given visitId
        List<PaymentDTO> paymentList = paymentsService.getPaymentsByVisitId(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, paymentList)).build();
    }

    @DELETE
    @Path("/delete-payment-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete payment by id", description = "delete payment by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deletePaymentById(@PathParam("id") Long id){
        return paymentsService.deletePayment(id);
    }

    @PUT
    @Path("/update-payment-id/{id}")
    @Transactional
    @Operation(summary = "update payment by id", description = "Update or reverse a payment by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PaymentDTO.class)))
    public Response updatePaymentById(@PathParam("id") Long id, PaymentRequest request) {
        return paymentsService.updatePayment(id, request);
    }


    @GET
    @Path("/get-payment-advanced-search")
    //@RolesAllowed({"ADMIN","USER","AGENT"})
    @Operation(summary = "get payment advanced search", description = "get payment advanced search.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientVisit.class)))
    public Response getPaymentAdvancedFilter(@BeanParam PaymentParametersRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,paymentsService.getPaymentsAdvancedFilter(request))).build();
    }

}






