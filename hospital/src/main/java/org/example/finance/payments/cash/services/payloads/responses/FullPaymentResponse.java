package org.example.finance.payments.cash.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class FullPaymentResponse {
    public Long id;
    public Long invoiceId;
    public Long visitId;
    public String paymentForm;
    public String notes;
    public String status;
    public String receivedBy;
    public BigDecimal amountToPay;
    public String paidBy;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfPayment;
    
    public LocalTime timeOfPayment;
}










