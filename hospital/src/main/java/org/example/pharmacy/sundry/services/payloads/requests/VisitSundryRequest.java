package org.example.pharmacy.sundry.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class VisitSundryRequest {

    @Schema(description = "Pharmacy stock batch to deduct from (when pharmacyUseStockBatch is true)")
    public Long stockBatchId;

    @Schema(description = "Shop/stock item to deduct from (when pharmacyUseStockBatch is false)")
    public Long itemId;

    @Schema(description = "Quantity to use", example = "2")
    public BigDecimal quantity;

    @Schema(description = "Staff member recording the sundry use")
    public String usedBy;
}
