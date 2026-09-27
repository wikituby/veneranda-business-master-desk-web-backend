package org.example.inventory.stock.services.payloads.requests;

import java.util.List;

public class PurchaseOrderRequest {
    public Long supplierId;
    public String supplierName;
    public String notes;
    public List<PurchaseOrderLineRequest> lines;
}
