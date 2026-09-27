package org.example.inventory.stock.services.payloads.requests;

import java.math.BigDecimal;

public class PurchaseOrderLineRequest {
    public Long id;
    public Long stockItemId;
    public String stockItemName;
    public String storeName;
    public String batchNumber;
    public BigDecimal inStock;
    public BigDecimal reorderTo;
    public BigDecimal quantityOrdered;
    public BigDecimal unitPrice;
    public String lineNotes;
}
