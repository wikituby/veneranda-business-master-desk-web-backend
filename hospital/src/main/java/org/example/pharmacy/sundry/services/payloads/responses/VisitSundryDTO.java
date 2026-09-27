package org.example.pharmacy.sundry.services.payloads.responses;

import org.example.pharmacy.sundry.domains.VisitSundry;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VisitSundryDTO {
    public Long id;
    public Long patientVisitId;
    public Long stockBatchId;
    public Long itemId;
    public String itemName;
    public String unitOfMeasure;
    public BigDecimal quantityUsed;
    public BigDecimal unitSellingPrice;
    public BigDecimal unitCostPrice;
    public BigDecimal lineTotal;
    public String usedBy;
    public LocalDateTime recordedAt;
    public Boolean dispensed;

    public VisitSundryDTO() {
    }

    public VisitSundryDTO(VisitSundry row) {
        if (row == null) {
            return;
        }
        this.id = row.id;
        this.patientVisitId = row.patientVisitId;
        this.stockBatchId = row.stockBatchId;
        this.itemId = row.itemId;
        this.itemName = row.itemName;
        this.unitOfMeasure = row.unitOfMeasure;
        this.quantityUsed = row.quantityUsed;
        this.unitSellingPrice = row.unitSellingPrice;
        this.unitCostPrice = row.unitCostPrice;
        this.lineTotal = row.lineTotal;
        this.usedBy = row.usedBy;
        this.recordedAt = row.recordedAt;
        this.dispensed = Boolean.TRUE.equals(row.dispensed);
    }
}
