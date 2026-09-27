package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.StockInitialization;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockInitializationDTO {
    public Long id;
    public Long stockBatchId;
    public Long stockItemId;
    public Long itemId;
    public String itemName;
    public Long storeId;
    public String storeName;
    public BigDecimal draftQuantity;
    public BigDecimal initialQuantity;
    public BigDecimal currentStockAtHand;
    public boolean finalized;
    public Long performedByUserId;
    public String performedByUserName;
    public LocalDateTime finalizedAt;
    public LocalDateTime updatedAt;

    public static StockInitializationDTO fromRecord(StockInitialization record) {
        StockInitializationDTO dto = new StockInitializationDTO();
        dto.id = record.id;
        dto.stockBatchId = record.stockBatchId;
        dto.stockItemId = record.stockItemId;
        dto.itemId = record.itemId;
        dto.itemName = record.itemName;
        dto.storeId = record.storeId;
        dto.storeName = record.storeName;
        dto.draftQuantity = record.draftQuantity;
        dto.initialQuantity = record.initialQuantity;
        dto.finalized = record.finalized;
        dto.performedByUserId = record.performedByUserId;
        dto.performedByUserName = record.performedByUserName;
        dto.finalizedAt = record.finalizedAt;
        dto.updatedAt = record.updatedAt;
        return dto;
    }
}
