package org.example.inventory.stock.services.payloads.responses.dtos;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.inventory.stock.domains.StockVerification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockVerificationDTO {
    public Long id;
    public Long stockBatchId;
    public Long itemId;
    public Long stockItemId;
    public String stockItemName;
    public Long storeId;
    public String storeName;

    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime verificationDateTime;

    public BigDecimal systemQuantityAtVerification;
    public BigDecimal physicalQuantity;
    public BigDecimal addingQuantity;
    public BigDecimal deductingQuantity;
    public Integer addingTransactionCount;
    public Integer deductingTransactionCount;
    public BigDecimal expectedQuantity;
    public BigDecimal varianceQuantity;

    public Long verificationStatusId;
    public String verificationStatusCode;
    public String verificationStatusName;

    public Long verificationReasonId;
    public String verificationReasonCode;
    public String verificationReasonName;

    public String description;
    public Long performedByUserId;
    public String performedByUserName;
    public Long approvedByUserId;
    public String approvedByUserName;

    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime approvedAt;

    public Integer confidenceScore;
    public Long confidenceLevelId;
    public String confidenceLevelCode;
    public String confidenceLevelName;

    public StockVerificationDTO() {
    }

    public StockVerificationDTO(StockVerification v) {
        if (v == null) {
            return;
        }
        this.id = v.id;
        if (v.stockBatch != null) {
            this.stockBatchId = v.stockBatch.id;
        }
        this.itemId = v.itemId;
        this.stockItemId = v.stockItemId;
        this.stockItemName = v.stockItemName;
        this.storeId = v.storeId;
        this.storeName = v.storeName;
        this.verificationDateTime = v.verificationDateTime;
        this.systemQuantityAtVerification = v.systemQuantityAtVerification;
        this.physicalQuantity = v.physicalQuantity;
        this.addingQuantity = v.addingQuantity;
        this.deductingQuantity = v.deductingQuantity;
        this.addingTransactionCount = v.addingTransactionCount;
        this.deductingTransactionCount = v.deductingTransactionCount;
        this.expectedQuantity = v.expectedQuantity;
        this.varianceQuantity = v.varianceQuantity;
        if (v.verificationStatus != null) {
            this.verificationStatusId = v.verificationStatus.id;
            this.verificationStatusCode = v.verificationStatus.code;
            this.verificationStatusName = v.verificationStatus.name;
        }
        if (v.verificationReason != null) {
            this.verificationReasonId = v.verificationReason.id;
            this.verificationReasonCode = v.verificationReason.code;
            this.verificationReasonName = v.verificationReason.name;
        }
        this.description = v.description;
        this.performedByUserId = v.performedByUserId;
        this.performedByUserName = v.performedByUserName;
        this.approvedByUserId = v.approvedByUserId;
        this.approvedByUserName = v.approvedByUserName;
        this.approvedAt = v.approvedAt;
        this.confidenceScore = v.confidenceScore;
        if (v.confidenceLevel != null) {
            this.confidenceLevelId = v.confidenceLevel.id;
            this.confidenceLevelCode = v.confidenceLevel.code;
            this.confidenceLevelName = v.confidenceLevel.name;
        }
    }
}
