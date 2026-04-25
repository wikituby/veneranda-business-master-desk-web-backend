package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.StockAdjustment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockAdjustmentDTO {
    public Long id;
    public Long stockBatchId;
    public Long adjustmentTypeId;
    public String adjustmentTypeName;
    public String adjustmentTypeCode;
    public BigDecimal quantityBefore;
    public BigDecimal quantityChanged;
    public BigDecimal quantityAfter;
    public String reason;
    public LocalDateTime date;
    public String doneBy;

    public StockAdjustmentDTO(StockAdjustment entity) {
        this.id = entity.id;
        if (entity.stockBatch != null) {
            this.stockBatchId = entity.stockBatch.id;
        }
        if (entity.adjustmentType != null) {
            this.adjustmentTypeId = entity.adjustmentType.id;
            this.adjustmentTypeName = entity.adjustmentType.name;
            this.adjustmentTypeCode = entity.adjustmentType.code;
        }
        this.quantityBefore = entity.quantityBefore;
        this.quantityChanged = entity.quantityChanged;
        this.quantityAfter = entity.quantityAfter;
        this.reason = entity.reason;
        this.date = entity.date;
        this.doneBy = entity.doneBy;
    }
}
