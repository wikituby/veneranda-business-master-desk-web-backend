package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class StockVerification extends PanacheEntity {

    @ManyToOne(optional = true)
    @JoinColumn(name = "stock_batch_id", nullable = true)
    public StockBatch stockBatch;

    /** Shop / catalog item id when facility uses item mode (not stock batches). */
    @Column
    public Long itemId;

    @Column(nullable = false)
    public Long stockItemId;

    @Column
    public String stockItemName;

    @Column
    public Long storeId;

    @Column
    public String storeName;

    @Column(nullable = false)
    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime verificationDateTime;

    @Column(precision = 19, scale = 2)
    public BigDecimal systemQuantityAtVerification;

    @Column(precision = 19, scale = 2)
    public BigDecimal physicalQuantity;

    @Column(precision = 19, scale = 2)
    public BigDecimal addingQuantity;

    @Column(precision = 19, scale = 2)
    public BigDecimal deductingQuantity;

    @Column
    public Integer addingTransactionCount;

    @Column
    public Integer deductingTransactionCount;

    @Column(precision = 19, scale = 2)
    public BigDecimal expectedQuantity;

    @Column(precision = 19, scale = 2)
    public BigDecimal varianceQuantity;

    @ManyToOne
    @JoinColumn(name = "verification_status_id")
    public VerificationStatus verificationStatus;

    @ManyToOne
    @JoinColumn(name = "verification_reason_id")
    public StockVerificationReason verificationReason;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    public Long performedByUserId;

    @Column
    public String performedByUserName;

    @Column
    public Long approvedByUserId;

    @Column
    public String approvedByUserName;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime approvedAt;

    @Column
    public Integer confidenceScore;

    @ManyToOne
    @JoinColumn(name = "confidence_level_id")
    public StockConfidenceLevel confidenceLevel;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime createdAt;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime updatedAt;
}