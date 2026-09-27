package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.PurchaseOrderLine;

import java.math.BigDecimal;

public class PurchaseOrderLineDTO {
    public Long id;
    public Long purchaseOrderId;
    public Long stockItemId;
    public String stockItemName;
    public String storeName;
    public String batchNumber;
    public BigDecimal inStock;
    public BigDecimal reorderTo;
    public BigDecimal quantityOrdered;
    public BigDecimal unitPrice;
    public String lineNotes;

    public PurchaseOrderLineDTO() {
    }

    public PurchaseOrderLineDTO(PurchaseOrderLine line) {
        this.id = line.id;
        this.purchaseOrderId = line.purchaseOrderId;
        this.stockItemId = line.stockItemId;
        this.stockItemName = line.stockItemName;
        this.storeName = line.storeName;
        this.batchNumber = line.batchNumber;
        this.inStock = line.inStock;
        this.reorderTo = line.reorderTo;
        this.quantityOrdered = line.quantityOrdered;
        this.unitPrice = line.unitPrice;
        this.lineNotes = line.lineNotes;
    }
}
