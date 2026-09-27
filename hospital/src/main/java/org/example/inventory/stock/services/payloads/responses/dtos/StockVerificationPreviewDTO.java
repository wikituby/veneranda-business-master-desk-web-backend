package org.example.inventory.stock.services.payloads.responses.dtos;

import jakarta.json.bind.annotation.JsonbDateFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockVerificationPreviewDTO {
    public Long stockBatchId;
    public Long itemId;
    public Long stockItemId;
    public String stockItemName;
    public Long storeId;
    public String storeName;
    public String batchNumber;

    public BigDecimal currentSystemQuantity;

    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime lastVerificationDateTime;

    public BigDecimal lastPhysicalQuantity;
    public BigDecimal addingQuantity;
    public BigDecimal deductingQuantity;
    public Integer addingTransactionCount;
    public Integer deductingTransactionCount;
    public BigDecimal expectedQuantity;
    public Integer confidenceScore;
    public Long confidenceLevelId;
    public String confidenceLevelCode;
    public String confidenceLevelName;
    public Long suggestedStatusId;
    public String suggestedStatusCode;
    public String suggestedStatusName;
}
