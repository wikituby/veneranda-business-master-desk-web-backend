package org.example.inventory.stock.services.payloads.requests;

import java.math.BigDecimal;

public class StockTrackingRequest {
    public Long stockItemId;
    public BigDecimal stockBeforeTransaction;
    public String transactionType; // "IN" or "OUT"
    public BigDecimal quantityChanged;
    public BigDecimal stockAfterTransaction;
}
