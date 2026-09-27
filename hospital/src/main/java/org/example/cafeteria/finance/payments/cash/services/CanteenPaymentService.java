package org.example.cafeteria.finance.payments.cash.services;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.cafeteria.finance.invoice.domains.CanteenInvoice;
import org.example.cafeteria.finance.invoice.domains.repositories.CanteenInvoiceRepository;
import org.example.cafeteria.finance.invoice.services.CanteenInvoiceService;
import org.example.cafeteria.finance.payments.cash.domains.CanteenPayments;
import org.example.cafeteria.finance.payments.cash.domains.repository.CanteenPaymentsRepository;
import org.example.cafeteria.finance.payments.cash.services.payloads.requests.CanteenPaymentParametersRequest;
import org.example.cafeteria.finance.payments.cash.services.payloads.requests.CanteenPaymentRequest;
import org.example.cafeteria.finance.payments.cash.services.payloads.responses.CanteenPaymentDTO;
import org.example.cafeteria.finance.payments.cash.services.payloads.responses.FullCanteenPaymentResponse;
import org.example.cafeteria.sales.saleDay.domains.SaleDay;
import org.example.cafeteria.sales.saleDay.domains.repository.SaleDayRepository;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class CanteenPaymentService {

    @Inject
    CanteenPaymentsRepository canteenPaymentsRepository;

    @Inject
    CanteenInvoiceRepository canteenInvoiceRepository;

    @Inject
    CanteenInvoiceService canteenInvoiceService;

    @Inject
    Pool client;

    @Inject
    SaleDayRepository patientVisitRepository;

    @Transactional
    public Response createNewPayment(Long visitId, CanteenPaymentRequest request) {
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
        SaleDay visit = patientVisitRepository.findById(visitId);
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

        // Get the first invoice from the list (or handle multiple invoices as needed)
        CanteenInvoice invoice = visit.invoice.get(0); // Assuming visit.invoice is a List<Invoice>
        if (invoice == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Invoice not found for visit ID:"  + visitId, null))
                    .build();
        }

        if (invoice.balanceDue.compareTo(BigDecimal.ZERO) == 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("This invoice is fully paid", null))
                    .build();
        }

        if (request.amountToPay.compareTo(invoice.balanceDue) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("You are Receiving More than the Debt ", null))
                    .build();
        }



            //throw new IllegalArgumentException("Invoice not found for visit ID: " + request.visitId);

        // Create a new Payment entity with PENDING status
        CanteenPayments payment = new CanteenPayments();
        payment.visit = visit;
        payment.invoice = invoice;
        payment.amountToPay = request.amountToPay;
        payment.paymentForm = request.paymentForm;
        payment.dateOfPayment = java.time.LocalDate.now();
        payment.timeOfPayment = java.time.LocalTime.now();
        payment.status = request.status;
        payment.notes = request.notes;

        payment.paidBy = visit.patient.patientFirstName+ " " + visit.patient.patientSecondName;

        payment.receivedBy = request.receivedBy;

        // Persist the payment
        canteenPaymentsRepository.persist(payment);

        // Update the invoice amount paid
        canteenInvoiceService.updateInvoiceAmountPaid(invoice);

        // Return the PaymentDTO
        //return new PaymentDTO(payment);


        return Response.ok(new ResponseMessage("New payment made successfully", new CanteenPaymentDTO(payment))).build();

    }






    @Transactional
    public BigDecimal getTotalPaymentOfInvoice(Long invoiceId) {
        // Validate the invoice ID
        if (invoiceId == null) {
            throw new IllegalArgumentException("Invoice ID cannot be null.");
        }

        // Fetch payments associated with the given invoice ID
        List<CanteenPayments> paymentsMade = CanteenPayments.find(
                "invoice.id = ?1 ORDER BY id DESC",
                invoiceId
        ).list();

        // Calculate the total amount paid or return BigDecimal.ZERO if no payments exist
        return paymentsMade.stream()
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
        List<CanteenPayments> paymentsMade = CanteenPayments.find(
                "visit.id = ?1 ORDER BY id DESC",
                visitId
        ).list();

        // Calculate the total amount paid or return BigDecimal.ZERO if no payments exist
        return paymentsMade.stream()
                .map(payment -> payment.amountToPay != null ? payment.amountToPay : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public List<CanteenPaymentDTO> getPaymentsByVisitId(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is "labtest" and visit ID matches, ordered descending
        List<CanteenPayments> visitPayments = CanteenPayments.find(
                "visit.id = ?1", // Replace 'id' with your desired field for sorting
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return visitPayments.stream()
                .map(CanteenPaymentDTO::new)
                .toList();
    }

    @Transactional
    public Response deletePayment(Long id) {
        try {
            // Execute the custom SQL query to delete the payment
            int rowsDeleted = canteenPaymentsRepository.deletePaymentById(id);

            // Check if any rows were deleted
            if (rowsDeleted > 0) {
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




    public List<FullCanteenPaymentResponse> getPaymentsAdvancedFilter(CanteenPaymentParametersRequest request) {
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
        FROM CanteenPayments
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


    private FullCanteenPaymentResponse from(Row row){

        FullCanteenPaymentResponse response = new FullCanteenPaymentResponse();
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

    private StringJoiner getStringJoiner(CanteenPaymentParametersRequest request) {
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





