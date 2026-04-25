package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.ExpiryItemRegister;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpiryItemRegisterDTO {
    public Long id;
    public Long stockItemId;
    public Long stockBatchId;
    public String batchNumber;
    public BigDecimal stockAtHand;
    public LocalDate expiryDate;
    public LocalDateTime dateOfStockRemoval;
    public String removedBy;

    public ExpiryItemRegisterDTO(ExpiryItemRegister entity) {
        this.id = entity.id;
        this.stockItemId = entity.stockItemId;
        this.stockBatchId = entity.stockBatchId;
        this.batchNumber = entity.batchNumber;
        this.stockAtHand = entity.stockAtHand;
        this.expiryDate = entity.expiryDate;
        this.dateOfStockRemoval = entity.dateOfStockRemoval;
        this.removedBy = entity.removedBy;
    }
}
