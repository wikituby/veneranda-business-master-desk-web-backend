package org.example.finance.payments.cash.services;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.invoice.domains.repositories.InvoiceRepository;
import org.example.finance.payments.cash.domains.Payments;
import org.example.finance.payments.cash.domains.repository.PaymentsRepository;
import org.example.finance.payments.cash.services.payloads.responses.FullPaymentResponse;
import org.example.finance.payments.cash.services.payloads.responses.PayAllPatientDebtResponse;
import org.example.finance.payments.cash.services.payloads.responses.PaymentDTO;
import org.example.finance.payments.cash.services.payloads.requests.PaymentParametersRequest;
import org.example.finance.payments.cash.services.payloads.requests.PaymentRequest;
import org.example.client.domains.Patient;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.invoice.services.InvoiceService;
import org.example.visit.domains.PatientVisit;
import org.example.visit.domains.repositories.PatientVisitRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class PaymentService {

    @Inject
    PaymentsRepository paymentsRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    InvoiceService invoiceService;

    @Inject
    Pool client;

    @Inject
    PatientVisitRepository patientVisitRepository;

    @Transactional
    public Response createNewPayment(Long visitId, PaymentRequest request) {
        // Validate the request

        if (visitId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit ID cannot be null.",null))
                    .build();
            //throw new IllegalArgumentException("Visit ID cannot be null.");
        }
        if (request.amountToPay == null || request.amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Amount to pay must be greater than zero.",null))
                    .build();
            //throw new IllegalArgumentException("Amount to pay must be greater than zero.");
        }
        if (request.paymentForm == null || request.paymentForm.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Payment form cannot be null or empty",null))
                    .build();
            //throw new IllegalArgumentException("Payment form cannot be null or empty.");
        }

        // Validate that the visit exists
        PatientVisit visit = patientVisitRepository.findById(visitId);
        if (visit == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit not found for ID:"  + visitId, null))
                    .build();
            //throw new IllegalArgumentException("Visit not found for ID: " + request.visitId);
        }

        // Ensure visit.invoice is not null and contains at least one invoice
        if (visit.invoice == null || visit.invoice.isEmpty()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Invoice not found for visit ID:"  + visitId, null))
                    .build();
            //throw new IllegalArgumentException("No invoice found for visit ID: " + request.visitId);
        }

        // Refresh totals from line items (visit invoice may still show 0 balance until synced)
        invoiceService.syncInvoiceTotalsForVisit(visitId);
        visit = patientVisitRepository.findById(visitId);
        if (visit.invoice == null || visit.invoice.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Invoice not found for visit ID:"  + visitId, null))
                    .build();
        }

        Invoice invoice = visit.invoice.get(0);
        if (invoice == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Invoice not found for visit ID:"  + visitId, null))
                    .build();
        }

        if (invoice.balanceDue.compareTo(BigDecimal.ZERO) == 0) {
            if (invoice.subTotal != null && invoice.subTotal.compareTo(BigDecimal.ZERO) > 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("This invoice is fully paid", null))
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "Nothing to pay on this visit. Add services or treatments before recording payment.",
                            null))
                    .build();
        }

        if (request.amountToPay.compareTo(invoice.balanceDue) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("You are Receiving More than the Debt ", null))
                    .build();
        }
        //throw new IllegalArgumentException("Invoice not found for visit ID: " + request.visitId);
        // Create a new Payment entity with PENDING status
        Payments payment = new Payments();
        payment.visit = visit;
        payment.invoice = invoice;
        payment.amountToPay = request.amountToPay;
        payment.paymentForm = request.paymentForm;
        payment.dateOfPayment = request.dateOfPayment != null
                ? request.dateOfPayment
                : java.time.LocalDate.now();
        payment.timeOfPayment = request.timeOfPayment != null
                ? request.timeOfPayment
                : java.time.LocalTime.now().withNano(0);
        payment.status = request.status;
        payment.notes = request.notes;

        payment.paidBy = visit.patient.patientFirstName+ " " + visit.patient.patientSecondName;

        payment.receivedBy = request.receivedBy;

        // Persist the payment
        paymentsRepository.persist(payment);

        // Update the invoice amount paid
        invoiceService.updateInvoiceAmountPaid(invoice);

        // Return the PaymentDTO
        //return new PaymentDTO(payment);

        return Response.ok(new ResponseMessage("New payment made successfully", new PaymentDTO(payment))).build();

    }

    /**
     * Apply a lump-sum payment across the patient's visits, oldest visit first,
     * until the amount is exhausted. Each slice is recorded as its own payment on that visit.
     */
    @Transactional
    public Response payAllPatientDebt(Long patientId, PaymentRequest request) {
        if (patientId == null || patientId <= 0) {
            return badRequest("A valid patient id is required.");
        }
        if (request == null || request.amountToPay == null || request.amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            return badRequest("Amount to pay must be greater than zero.");
        }

        Patient patient = Patient.findById(patientId);
        if (patient == null) {
            return badRequest("Patient not found for ID: " + patientId);
        }

        String paymentForm = request.paymentForm != null && !request.paymentForm.isBlank()
                ? request.paymentForm
                : "Cash At Hand";
        String status = request.status != null && !request.status.isBlank()
                ? request.status
                : "Approved";
        String baseNotes = request.notes != null && !request.notes.isBlank()
                ? request.notes.trim()
                : "Pay all patient debt";

        List<PatientVisit> visits = PatientVisit.find(
                "patient.id = ?1 ORDER BY visitNumber ASC, id ASC",
                patientId
        ).list();

        BigDecimal remaining = request.amountToPay;
        PayAllPatientDebtResponse result = new PayAllPatientDebtResponse();
        result.totalApplied = BigDecimal.ZERO;
        result.remainingUnapplied = remaining;

        for (PatientVisit visit : visits) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            invoiceService.syncInvoiceTotalsForVisit(visit.id);
            PatientVisit freshVisit = patientVisitRepository.findById(visit.id);
            if (freshVisit == null || freshVisit.invoice == null || freshVisit.invoice.isEmpty()) {
                continue;
            }

            Invoice invoice = freshVisit.invoice.get(0);
            if (invoice == null || invoice.balanceDue == null || invoice.balanceDue.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal slice = remaining.min(invoice.balanceDue);
            PaymentRequest sliceRequest = new PaymentRequest();
            sliceRequest.amountToPay = slice;
            sliceRequest.paymentForm = paymentForm;
            sliceRequest.status = status;
            sliceRequest.receivedBy = request.receivedBy;
            String visitLabel = freshVisit.visitNumber > 0
                    ? "Visit " + freshVisit.visitNumber
                    : "Visit #" + freshVisit.id;
            sliceRequest.notes = baseNotes + " (" + visitLabel + ")";

            Response paymentResponse = createNewPayment(freshVisit.id, sliceRequest);
            if (paymentResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return paymentResponse;
            }

            ResponseMessage body = (ResponseMessage) paymentResponse.getEntity();
            PaymentDTO paymentDto = body != null && body.data instanceof PaymentDTO
                    ? (PaymentDTO) body.data
                    : null;

            result.allocations.add(new PayAllPatientDebtResponse.PayAllPatientDebtAllocation(
                    freshVisit.id,
                    freshVisit.visitNumber,
                    slice,
                    paymentDto
            ));
            result.totalApplied = result.totalApplied.add(slice);
            remaining = remaining.subtract(slice);
        }

        if (result.allocations.isEmpty()) {
            // No visit invoices — allow paying pharmacy/OTC account debt on the patient.
            BigDecimal patientDue = patient.totalAmountDue != null
                    ? patient.totalAmountDue
                    : BigDecimal.ZERO;
            if (patientDue.compareTo(BigDecimal.ZERO) <= 0) {
                return badRequest("No outstanding visit balances found for this patient.");
            }
            BigDecimal slice = remaining.min(patientDue);
            patient.totalAmountDue = patientDue.subtract(slice);
            if (patient.totalAmountDue.compareTo(BigDecimal.ZERO) < 0) {
                patient.totalAmountDue = BigDecimal.ZERO;
            }
            patient.persist();
            result.totalApplied = slice;
            result.remainingUnapplied = remaining.subtract(slice).max(BigDecimal.ZERO);
            return Response.ok(new ResponseMessage(
                    "Patient account debt of " + slice + " paid.",
                    result
            )).build();
        }

        // Keep patient.totalAmountDue in sync for mixed visit + OTC debt.
        BigDecimal patientDue = patient.totalAmountDue != null
                ? patient.totalAmountDue
                : BigDecimal.ZERO;
        if (patientDue.compareTo(BigDecimal.ZERO) > 0 && result.totalApplied.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal nextDue = patientDue.subtract(result.totalApplied.min(patientDue));
            if (nextDue.compareTo(BigDecimal.ZERO) < 0) {
                nextDue = BigDecimal.ZERO;
            }
            // If payment still remains and patient still has OTC-only debt, apply the rest.
            if (remaining.compareTo(BigDecimal.ZERO) > 0 && nextDue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal otcSlice = remaining.min(nextDue);
                nextDue = nextDue.subtract(otcSlice);
                result.totalApplied = result.totalApplied.add(otcSlice);
                remaining = remaining.subtract(otcSlice);
            }
            patient.totalAmountDue = nextDue;
            patient.persist();
        }

        result.remainingUnapplied = remaining.max(BigDecimal.ZERO);
        return Response.ok(new ResponseMessage(
                "Patient debt payment applied across " + result.allocations.size() + " visit(s).",
                result
        )).build();
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseMessage(message, null))
                .build();
    }

    /**
     * After pharmacy dispenses visit prescriptions at the counter, apply payment to the visit invoice
     * (up to the sale total and current balance due). No-op when the invoice is already settled.
     */
    @Transactional
    public boolean tryRecordPharmacyDispensePayment(Long visitId, BigDecimal pharmacySaleTotal, PaymentRequest template) {
        if (visitId == null || pharmacySaleTotal == null || pharmacySaleTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        // "Dispensed, no money received" must never create a visit payment row.
        if (org.example.finance.billing.services.PatientBillingService.isNoPaymentTemplate(template)) {
            return false;
        }
        invoiceService.syncInvoiceTotalsForVisit(visitId);
        PatientVisit visit = patientVisitRepository.findById(visitId);
        if (visit == null || visit.invoice == null || visit.invoice.isEmpty()) {
            return false;
        }
        Invoice invoice = visit.invoice.get(0);
        if (invoice == null || invoice.balanceDue == null || invoice.balanceDue.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        PaymentRequest payment = new PaymentRequest();
        payment.amountToPay = pharmacySaleTotal.min(invoice.balanceDue);
        payment.paymentForm = template != null && template.paymentForm != null && !template.paymentForm.isBlank()
                ? template.paymentForm
                : "Cash At Hand";
        payment.status = template != null && template.status != null && !template.status.isBlank()
                ? template.status
                : "Approved";
        payment.notes = template != null && template.notes != null && !template.notes.isBlank()
                ? template.notes
                : "Pharmacy prescription payment";
        payment.receivedBy = template != null ? template.receivedBy : null;
        Response response = createNewPayment(visitId, payment);
        return response.getStatus() == Response.Status.OK.getStatusCode();
    }

    @Transactional
    public BigDecimal getTotalPaymentOfInvoice(Long invoiceId) {
        // Validate the invoice ID
        if (invoiceId == null) {
            throw new IllegalArgumentException("Invoice ID cannot be null.");
        }

        // Fetch payments associated with the given invoice ID
        List<Payments> paymentsMade = Payments.find(
                "invoice.id = ?1 ORDER BY id DESC",
                invoiceId
        ).list();

        // Calculate the total amount paid or return BigDecimal.ZERO if no payments exist
        return paymentsMade.stream()
                .filter(payment -> !isReversedPayment(payment))
                .map(payment -> payment.amountToPay != null ? payment.amountToPay : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Transactional
    public BigDecimal getTotalPaymentOfVisit(Long visitId) {
        // Validate the invoice ID
        if (visitId == null) {
            throw new IllegalArgumentException("visitId ID cannot be null.");
        }

        // Fetch payments associated with the given invoice ID
        List<Payments> paymentsMade = Payments.find(
                "visit.id = ?1 ORDER BY id DESC",
                visitId
        ).list();

        // Calculate the total amount paid or return BigDecimal.ZERO if no payments exist
        return paymentsMade.stream()
                .filter(payment -> !isReversedPayment(payment))
                .map(payment -> payment.amountToPay != null ? payment.amountToPay : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isReversedPayment(Payments payment) {
        return payment != null
                && payment.status != null
                && "reversed".equalsIgnoreCase(payment.status.trim());
    }


    public List<PaymentDTO> getPaymentsByVisitId(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is "labtest" and visit ID matches, ordered descending
        List<Payments> visitPayments = Payments.find(
                "visit.id = ?1", // Replace 'id' with your desired field for sorting
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return visitPayments.stream()
                .map(PaymentDTO::new)
                .toList();
    }

    @Transactional
    public Response updatePayment(Long id, PaymentRequest request) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Payment ID cannot be null.", null))
                    .build();
        }
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Payment request cannot be null.", null))
                    .build();
        }

        Payments payment = Payments.findById(id);
        if (payment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Payment not found", null))
                    .build();
        }
        if (payment.visit != null && "closed".equalsIgnoreCase(String.valueOf(payment.visit.visitStatus))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot update this payment.", null))
                    .build();
        }
        if (isReversedPayment(payment)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Reversed payments cannot be edited.", null))
                    .build();
        }

        boolean reversing = request.status != null && "reversed".equalsIgnoreCase(request.status.trim());
        if (reversing) {
            payment.status = "Reversed";
        } else {
            if (request.amountToPay != null) {
                if (request.amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ResponseMessage("Amount to pay must be greater than zero.", null))
                            .build();
                }
                payment.amountToPay = request.amountToPay;
            }
            if (request.paymentForm != null && !request.paymentForm.isBlank()) {
                payment.paymentForm = request.paymentForm.trim();
            }
            if (request.notes != null) {
                payment.notes = request.notes;
            }
            if (request.status != null && !request.status.isBlank()) {
                payment.status = request.status.trim();
            }
            if (request.receivedBy != null && !request.receivedBy.isBlank()) {
                payment.receivedBy = request.receivedBy.trim();
            }
            if (request.dateOfPayment != null) {
                payment.dateOfPayment = request.dateOfPayment;
            }
            if (request.timeOfPayment != null) {
                payment.timeOfPayment = request.timeOfPayment;
            }
        }

        paymentsRepository.persist(payment);
        if (payment.invoice != null) {
            invoiceService.updateInvoiceAmountPaid(payment.invoice);
        }

        String message = reversing ? "Payment reversed successfully" : "Payment updated successfully";
        return Response.ok(new ResponseMessage(message, new PaymentDTO(payment))).build();
    }

    @Transactional
    public Response deletePayment(Long id) {
        try {
            Payments payment = Payments.findById(id);
            if (payment == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Payment not found", null))
                        .build();
            }

            if (payment.visit != null && "closed".equals(payment.visit.visitStatus)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null))
                        .build();
            }

            Invoice invoice = payment.invoice;

            // Execute the custom SQL query to delete the payment
            int rowsDeleted = paymentsRepository.deletePaymentById(id);

            // Check if any rows were deleted
            if (rowsDeleted > 0) {
                if (invoice != null) {
                    invoiceService.updateInvoiceAmountPaid(invoice);
                }
                return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Payment not found", null))
                        .build();
            }
        } catch (Exception e) {
            // Log the error and return a 500 response
            System.err.println("Error deleting payment: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage("Failed to delete payment: " + e.getMessage(), null))
                    .build();
        }
    }




    public List<FullPaymentResponse> getPaymentsAdvancedFilter(PaymentParametersRequest request) {
        StringJoiner whereClause = getStringJoiner(request);

        String sql = """
        SELECT
            id,
            invoice_id,
            visit_id,
            paymentForm,
            notes,
            status,
            receivedBy,
            amountToPay,
            dateOfPayment,
            paidBy,
            timeOfPayment
        FROM Payments
        %s
        ORDER BY dateOfPayment DESC;
        """.formatted(whereClause);

        return client.query(sql)
                .execute()
                .onItem()
                .transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem()
                .transform(this::from)
                .collect().asList()
                .await()
                .indefinitely();
    }


    private FullPaymentResponse from(Row row){

        FullPaymentResponse response = new FullPaymentResponse();
        response.id = row.getLong("id");
        response.invoiceId = row.getLong("invoice_id");
        response.visitId = row.getLong("visit_id");
        response.paymentForm = row.getString("paymentForm");
        response.notes = row.getString("notes");
        response.status = row.getString("status");
        response.receivedBy = row.getString("receivedBy");
        response.amountToPay = row.getBigDecimal("amountToPay");
        response.paidBy = row.getString("paidBy");
        response.dateOfPayment = row.getLocalDate("dateOfPayment");
        response.timeOfPayment = row.getLocalTime("timeOfPayment");

        return response;
    }

    private StringJoiner getStringJoiner(PaymentParametersRequest request) {
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

        List<String> conditions = new ArrayList<>();
        if (request.paymentForm != null && !request.paymentForm.isEmpty()) {
            conditions.add("paymentForm = '" + request.paymentForm + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.receivedBy != null && !request.receivedBy.isEmpty()) {
            conditions.add("receivedBy = '" + request.receivedBy + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.paidBy != null && !request.paidBy.isEmpty()) {
            conditions.add("paidBy = '" + request.paidBy + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.datefrom != null && request.dateto != null) {
            conditions.add("dateOfPayment BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

        conditions.forEach(whereClause::add);

        if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
            whereClause.add("1 = 1");
        }

        return whereClause;
    }


















}





