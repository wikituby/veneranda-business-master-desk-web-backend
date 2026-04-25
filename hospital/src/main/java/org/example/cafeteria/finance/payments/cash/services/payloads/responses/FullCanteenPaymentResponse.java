package org.example.cafeteria.finance.payments.cash.services.payloads.responses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class FullCanteenPaymentResponse {

    public Long id;
    public Long invoiceId;
    public Long visitId;
    public BigDecimal amountToPay;
    public String paymentForm;
    public String notes;
    public String status;
    public String paidBy;
    public String receivedBy;
    public LocalDate dateOfPayment;
    public LocalTime timeOfPayment;

}






