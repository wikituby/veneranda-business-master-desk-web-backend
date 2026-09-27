package org.example.inventory.stock.services.payloads.requests;

import java.math.BigDecimal;

public class StockVerificationRequest {
    public Long stockBatchId;
    /** Used when facility inventory mode is shop items (not stock batches). */
    public Long itemId;
    public BigDecimal physicalQuantity;
    public Long verificationStatusId;
    public Long verificationReasonId;
    public String description;
    public Long performedByUserId;
    public String performedByUserName;
}