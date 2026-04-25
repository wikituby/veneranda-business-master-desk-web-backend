package org.example.cafeteria.finance.payments.cash.services.payloads.responses;

import org.example.cafeteria.finance.payments.cash.domains.CanteenPayments;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class CanteenPaymentDTO {

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

    public CanteenPaymentDTO(CanteenPayments payments) {
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

    // Getters and Setters


    public LocalDate getDateOfPayment() {
        return dateOfPayment;
    }

    public void setDateOfPayment(LocalDate dateOfPayment) {
        this.dateOfPayment = dateOfPayment;
    }


    public LocalTime getTimeOfPayment() {
        return timeOfPayment;
    }

    public void setTimeOfPayment(LocalTime timeOfPayment) {
        this.timeOfPayment = timeOfPayment;
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

    public void setAmountToPay(BigDecimal amountToPay) {
        this.amountToPay = amountToPay;
    }

    public String getPaymentForm() {
        return paymentForm;
    }

    public void setPaymentForm(String paymentForm) {
        this.paymentForm = paymentForm;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}






