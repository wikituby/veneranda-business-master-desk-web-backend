package org.example.inventory.stock.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class StockTransferRequest {

    @Schema(example = "1")
    public Long stockBatchId;

    @Schema(example = "1")
    public Long fromStoreId;

    @Schema(example = "1")
    public Long toStoreId;

    @Schema(example = "5")
    public BigDecimal qty;

    @Schema(example = "admin")
    public String transferredBy;
}
