package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.StockBatch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StockBatchDTO {

    public Long id;
    public String stockItemName;
    public Long storeId;
    public String storeName;
    public Long stockItemId;
    public BigDecimal unitCostPrice;
    public BigDecimal unitSellingPrice;
    public BigDecimal stockAtHand;
    public Integer shelfNumber;
    public BigDecimal profitMargin;
    public Integer reOrderLevel;
    public Integer reOrderTo;
    public Integer reOrderQuantity;
    public String unitOfMeasure;
    public BigDecimal lastUnitValue;
    public String lastUnitOfMeasure;
    public String batchNumber;
    public Long stockSupplierId;
    public String stockSupplierName;
    public String packaging;
    public LocalDateTime creationDateAndTime;
    public LocalDateTime upDateDateAndTime;
    public LocalDate expiryDate;

    public StockBatchDTO(StockBatch entity) {
        this.id = entity.id;
        this.reOrderTo = entity.reOrderTo;
        this.stockItemName = entity.stockItemName;
        this.storeId = entity.storeId;
        this.storeName = entity.storeName;
        this.stockItemId = entity.stockItemId;
        this.unitCostPrice = entity.unitCostPrice;
        this.unitSellingPrice = entity.unitSellingPrice;
        this.stockAtHand = entity.stockAtHand;
        this.shelfNumber = entity.shelfNumber;
        this.profitMargin = entity.profitMargin;
        this.reOrderLevel = entity.reOrderLevel;
        this.reOrderQuantity = entity.reOrderQuantity;
        this.unitOfMeasure = entity.unitOfMeasure;
        this.lastUnitValue = entity.lastUnitValue;
        this.lastUnitOfMeasure = entity.lastUnitOfMeasure;
        this.batchNumber = entity.batchNumber;
        this.stockSupplierId = entity.stockSupplierId;
        this.stockSupplierName = entity.stockSupplierName;
        this.packaging = entity.packaging;
        this.creationDateAndTime = entity.creationDateAndTime;
        this.upDateDateAndTime = entity.upDateDateAndTime;
        this.expiryDate = entity.expiryDate;
    }
}
