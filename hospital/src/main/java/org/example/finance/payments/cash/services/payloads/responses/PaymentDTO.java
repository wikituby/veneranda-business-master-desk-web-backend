package org.example.finance.payments.cash.services.payloads.responses;

import org.example.finance.payments.cash.domains.Payments;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class PaymentDTO {

    public Long id;
    public Long invoiceId;
    public Long visitId;
    public BigDecimal amountToPay;
    public String paymentForm;
    public String notes;
    public String status;
    public LocalDate dateOfPayment;
    public LocalTime timeOfPayment;
    public String paidBy;
    public String receivedBy;

    public PaymentDTO(Payments payments) {
        this.id = payments.id;
        this.paidBy = payments.paidBy;
        this.receivedBy = payments.receivedBy;
        this.invoiceId = payments.invoice != null ? payments.invoice.id : null;
        this.visitId = payments.visit != null ? payments.visit.id : null;
        this.amountToPay = payments.amountToPay;
        this.paymentForm = payments.paymentForm;
        this.notes = payments.notes;
        this.status = payments.status;
        this.dateOfPayment = payments.dateOfPayment;
        this.timeOfPayment = payments.timeOfPayment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmountToPay() {
        return amountToPay;
    }
}










