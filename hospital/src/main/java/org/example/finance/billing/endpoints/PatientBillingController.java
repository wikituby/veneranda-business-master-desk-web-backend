package org.example.finance.billing.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.billing.services.PatientBillingService;
import org.example.finance.invoice.services.InvoiceService;
import java.util.Map;

@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient billing", description = "Visit invoice sync and pharmacy settlement")
public class PatientBillingController {

    @Inject
    InvoiceService invoiceService;

    @Inject
    PatientBillingService patientBillingService;

    /**
     * Recompute visit invoice from procedures and dispensed treatments (given/dispensed only for pharmacy lines).
     */
    @POST
    @Path("sync-visit-billing/{visitId}")
    @Transactional
    @Operation(summary = "Sync visit billing totals from all billable line items")
    public Response syncVisitBilling(@PathParam("visitId") Long visitId) {
        if (visitId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit id is required.", null))
                    .build();
        }
        Map<String, java.math.BigDecimal> totals = invoiceService.getInvoiceSubTotal(visitId);
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, totals)).build();
    }
}
