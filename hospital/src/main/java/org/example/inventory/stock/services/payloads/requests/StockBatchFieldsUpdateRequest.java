package org.example.inventory.stock.services.payloads.requests;

import java.math.BigDecimal;

public class StockBatchFieldsUpdateRequest {
    /** When set (e.g. after expiry register / write-off), updates on-hand quantity. */
    public BigDecimal stockAtHand;
    public BigDecimal profitMargin;
    public Integer reOrderLevel;
    public Integer reOrderQuantity;
    public Integer reOrderTo;
    public String unitOfMeasure;
    public BigDecimal lastUnitValue;
    public String lastUnitOfMeasure;
}















