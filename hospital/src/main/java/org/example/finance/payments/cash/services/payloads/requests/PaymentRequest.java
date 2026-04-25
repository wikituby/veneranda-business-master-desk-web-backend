package org.example.finance.payments.cash.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class PaymentRequest {
    @Schema(example = "50000")
    public BigDecimal amountToPay;

    @Schema(example = "cash at hand")
    public String paymentForm;

    @Schema(example = "approved")
    public String status;

    @Schema(example = "Payment received")
    public String notes;

    @Schema(example = "John Doe")
    public String receivedBy;
}










