package org.example.inventory.stock.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpiryItemRegisterRequest {

    @Schema(description = "Stock item id", example = "1")
    public Long stockItemId;

    @Schema(description = "StockBatch id (preferred). If omitted, batchNumber and expiryDate must match a single batch.")
    public Long stockBatchId;

    @Schema(example = "BATCH-2024-001")
    public String batchNumber;

    @Schema(description = "Quantity at hand at time of removal", example = "12.5")
    public BigDecimal stockAtHand;

    @Schema(description = "Batch expiry date")
    public LocalDate expiryDate;

    @Schema(description = "When stock was removed from shelf / written off")
    public LocalDateTime dateOfStockRemoval;

    @Schema(description = "Username or staff who performed removal", example = "nurse.jane")
    public String removedBy;
}
