package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.StockTracking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockTrackingDTO {
    public Long id;
    public LocalDateTime recordedAt;
    public Long stockItemId;
    public BigDecimal stockBeforeTransaction;
    public String transactionType;
    public BigDecimal quantityChanged;
    public BigDecimal stockAfterTransaction;
    public Long stockBatchId;
    public Long storeId;
    public String sourceEvent;
    public Long referenceId;
    public String referenceType;

    public StockTrackingDTO(StockTracking entity) {
        this.id = entity.id;
        this.recordedAt = entity.recordedAt;
        this.stockItemId = entity.stockItemId;
        this.stockBeforeTransaction = entity.stockBeforeTransaction;
        this.transactionType = entity.transactionType;
        this.quantityChanged = entity.quantityChanged;
        this.stockAfterTransaction = entity.stockAfterTransaction;
        this.stockBatchId = entity.stockBatchId;
        this.storeId = entity.storeId;
        this.sourceEvent = entity.sourceEvent;
        this.referenceId = entity.referenceId;
        this.referenceType = entity.referenceType;
    }
}
