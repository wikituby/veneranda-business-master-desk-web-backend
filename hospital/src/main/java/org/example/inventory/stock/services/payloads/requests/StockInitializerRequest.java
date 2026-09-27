package org.example.inventory.stock.services.payloads.requests;

import java.math.BigDecimal;

public class StockInitializerRequest {
    public Long stockBatchId;
    public Long stockItemId;
    public Long itemId;
    public Long storeId;
    public BigDecimal quantity;
    public Long performedByUserId;
    public String performedByUserName;
}
