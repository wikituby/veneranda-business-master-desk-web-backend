package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.StockTransfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockTransferDTO {

    public Long id;
    public Long stockBatchId;
    public Long fromStoreId;
    public Long toStoreId;
    public BigDecimal quantity;
    public String transferredBy;
    public LocalDateTime transferDate;

    public StockTransferDTO(StockTransfer stockTransfer) {
        this.id = stockTransfer.id;
        this.stockBatchId = stockTransfer.stockBatchId;
        this.fromStoreId = stockTransfer.fromStoreId;
        this.toStoreId = stockTransfer.toStoreId;
        this.quantity = stockTransfer.quantity;
        this.transferredBy = stockTransfer.transferredBy;
        this.transferDate = stockTransfer.transferDate;
    }
}
