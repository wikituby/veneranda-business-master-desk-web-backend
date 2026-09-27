package org.example.inventory.stock.services.payloads.requests;

import jakarta.json.bind.annotation.JsonbDateFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockBatchFieldsUpdateRequest {
    /** When set (e.g. after expiry register / write-off), updates on-hand quantity. */
    public BigDecimal stockAtHand;
    public BigDecimal unitCostPrice;
    public BigDecimal unitSellingPrice;
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;
    public Integer shelfNumber;
    public BigDecimal profitMarginForRetail;
    public BigDecimal profitMarginForWholeSale;
    public BigDecimal profitMarginForSpecialCase;
    public Long unitSellingModelId;
    public Integer reOrderLevel;
    public Integer reOrderQuantity;
    public Integer reOrderTo;
    public String unitOfMeasure;
    public BigDecimal lastUnitValue;
    public String lastUnitOfMeasure;
}















