package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.StockReceive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StockReceiveDTO {

    public Long id;
    public Long stockBatchId;
    public String stockItemName;
    public Long storeId;
    public String storeName;
    public BigDecimal quantityReceived;
    public BigDecimal unitCostPrice;
    public BigDecimal totalCostPrice;
    public BigDecimal unitSellingPrice;
    public BigDecimal quantityAvailable;
    public BigDecimal newQuantity;
    public LocalDate receiveDate;
    public LocalDate expiryDate;
    public String supplierName;
    public Long supplierId;
    public String invoiceNumber;
    public String batchNumber;
    public String packaging;

    public StockReceiveDTO(StockReceive entity) {
        this.id = entity.id;
        this.stockBatchId = entity.stockBatchId;
        this.stockItemName = entity.stockItemName;
        this.storeId = entity.storeId;
        this.storeName = entity.storeName;
        this.quantityReceived = entity.quantityReceived;
        this.unitCostPrice = entity.unitCostPrice;
        this.totalCostPrice = entity.totalCostPrice;
        this.unitSellingPrice = entity.unitSellingPrice;
        this.quantityAvailable = entity.quantityAvailable;
        this.newQuantity = entity.newQuantity;
        this.receiveDate = entity.receiveDate;
        this.expiryDate = entity.expiryDate;
        this.supplierName = entity.supplierName;
        this.supplierId = entity.supplierId;
        this.invoiceNumber = entity.invoiceNumber;
        this.batchNumber = entity.batchNumber;
        this.packaging = entity.packaging;
    }
}
