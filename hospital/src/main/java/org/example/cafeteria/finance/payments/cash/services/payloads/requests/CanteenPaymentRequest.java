package org.example.cafeteria.finance.payments.cash.services.payloads.requests;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class CanteenPaymentRequest {



    @Schema(example = "15000.00")
    public BigDecimal amountToPay;

    @Schema(example = "cash at hand")
    public String paymentForm; // e.g., "cash at hand or cash at bank"

    @Schema(example = "Payment Done")
    public String notes;

    @Schema(example = "Approved")
    public String status;

    @Schema(example = "cryton")
    public String receivedBy;
}






