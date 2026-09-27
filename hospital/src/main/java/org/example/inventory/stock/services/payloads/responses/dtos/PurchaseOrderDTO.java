package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.PurchaseOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDTO {
    public Long id;
    public String orderNo;
    public Long supplierId;
    public String supplierName;
    public String notes;
    public String status;
    public LocalDateTime createdAt;
    public List<PurchaseOrderLineDTO> lines = new ArrayList<>();

    public PurchaseOrderDTO() {
    }

    public PurchaseOrderDTO(PurchaseOrder order) {
        this.id = order.id;
        this.orderNo = order.orderNo;
        this.supplierId = order.supplierId;
        this.supplierName = order.supplierName;
        this.notes = order.notes;
        this.status = order.status;
        this.createdAt = order.createdAt;
    }
}
