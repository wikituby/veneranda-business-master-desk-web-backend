package org.example.inventory.stock.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockAdjustmentRequest {

    @Schema(description = "StockBatch id", required = true)
    public Long stockBatchId;

    @Schema(description = "AdjustmentType id", required = true)
    public Long adjustmentTypeId;

    @Schema(description = "Signed change: positive adds stock, negative removes", example = "-5")
    public BigDecimal quantityChanged;

    @Schema(example = "Broken vials during handling")
    public String reason;

    @Schema(description = "When the adjustment was done (defaults to now)")
    public LocalDateTime date;

    @Schema(example = "store.manager")
    public String doneBy;
}
