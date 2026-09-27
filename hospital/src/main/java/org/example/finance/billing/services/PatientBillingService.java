package org.example.finance.billing.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.finance.invoice.services.InvoiceService;
import org.example.finance.payments.cash.services.PaymentService;
import org.example.finance.payments.cash.services.payloads.requests.PaymentRequest;
import org.example.treatment.domains.TreatmentRequested;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.util.List;

/**
 * Visit-level patient billing: invoice sync, pharmacy dispense settlement (given + payment).
 */
@ApplicationScoped
public class PatientBillingService {

    @Inject
    InvoiceService invoiceService;

    @Inject
    PaymentService paymentService;

    /**
     * Pharmacy / treatment lines count toward the visit invoice when PD (paid) is set.
     * Legacy rows with a null paid flag are still included. Cancelled lines are excluded.
     */
    public static boolean countsTowardVisitInvoice(TreatmentRequested treatment) {
        return treatment != null && treatment.countsTowardInvoice();
    }

    /** Sell amount for one treatment line (uses qty × sell price when total is missing). */
    public static BigDecimal treatmentSellingAmount(TreatmentRequested treatment) {
        if (treatment == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = treatment.totalAmount != null ? treatment.totalAmount : BigDecimal.ZERO;
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            return total;
        }
        BigDecimal qty = treatment.quantity != null ? treatment.quantity : BigDecimal.ZERO;
        BigDecimal price = treatment.unitSellingPrice != null ? treatment.unitSellingPrice : BigDecimal.ZERO;
        return qty.multiply(price);
    }

    @Transactional
    public void markTreatmentGiven(Long treatmentId, Long visitId) {
        markTreatmentDispensed(treatmentId, visitId, true);
    }

    /**
     * Mark a treatment line dispensed. {@code paid} should be true only when money was collected.
     */
    @Transactional
    public void markTreatmentDispensed(Long treatmentId, Long visitId, boolean paid) {
        if (treatmentId == null) {
            return;
        }
        TreatmentRequested treatment = TreatmentRequested.findById(treatmentId);
        if (treatment == null || treatment.visit == null) {
            return;
        }
        if (visitId != null && !visitId.equals(treatment.visit.id)) {
            return;
        }
        treatment.status = "given";
        treatment.paid = paid;
        treatment.dispensed = true;
        treatment.persist();
    }

    /**
     * After pharmacy completes a prescription sale: mark lines given, refresh invoice totals, record payment.
     *
     * @return true when a visit payment row was created
     */
    @Transactional
    public boolean settlePharmacyDispense(
            Long visitId,
            List<Long> treatmentRequestedIds,
            BigDecimal pharmacySaleTotal,
            PaymentRequest paymentTemplate) {
        if (visitId == null) {
            return false;
        }
        boolean collectPayment = !isNoPaymentTemplate(paymentTemplate);
        if (treatmentRequestedIds != null) {
            for (Long id : treatmentRequestedIds) {
                markTreatmentDispensed(id, visitId, collectPayment);
            }
        }
        invoiceService.syncInvoiceTotalsForVisit(visitId);
        if (!collectPayment) {
            return false;
        }
        return paymentService.tryRecordPharmacyDispensePayment(visitId, pharmacySaleTotal, paymentTemplate);
    }

    /** Refresh visit invoice totals after a dispense that did not collect payment. */
    @Transactional
    public void syncInvoiceAfterPharmacyDispense(Long visitId) {
        if (visitId == null) {
            return;
        }
        invoiceService.syncInvoiceTotalsForVisit(visitId);
    }

    public static PaymentRequest pharmacyPaymentTemplate(
            String paymentForm,
            String receivedBy,
            String notes) {
        PaymentRequest payment = new PaymentRequest();
        payment.paymentForm = paymentForm != null && !paymentForm.isBlank() ? paymentForm : "Cash At Hand";
        payment.status = "Approved";
        payment.notes = notes != null && !notes.isBlank() ? notes : "Pharmacy prescription sale";
        payment.receivedBy = receivedBy;
        return payment;
    }

    /** True when the client asked to dispense without collecting money. */
    public static boolean isNoPaymentTemplate(PaymentRequest template) {
        if (template == null) {
            return false;
        }
        String form = template.paymentForm != null ? template.paymentForm.trim() : "";
        if (form.equalsIgnoreCase("Not paid") || form.equalsIgnoreCase("Notpaid")) {
            return true;
        }
        String notes = template.notes != null ? template.notes.toLowerCase() : "";
        return notes.contains("no payment")
                || notes.contains("money not received")
                || notes.contains("not received");
    }

    public static boolean visitIsOpen(PatientVisit visit) {
        return visit != null && !"closed".equals(visit.visitStatus);
    }
}
