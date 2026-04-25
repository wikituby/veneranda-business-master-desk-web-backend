package org.example.cafeteria.finance.invoice.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class CanteenInvoiceUpdateRequest {

    @Schema(example = "1000")
    public BigDecimal tax;

    @Schema(example = "10000")
    public BigDecimal discount;

    @Schema(example = "Received with thanks")
    public String notes;
}






