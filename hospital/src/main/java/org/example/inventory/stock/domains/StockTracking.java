package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class StockTracking extends PanacheEntity {

    /** Server date and time when this movement was recorded. */
    @Column
    public LocalDateTime recordedAt;

    @Column(nullable = false)
    public Long stockItemId; // Reference to the specific stock item

    @Column(precision = 19, scale = 2)
    public BigDecimal stockBeforeTransaction; // Stock quantity before transaction

    @Column(nullable = false)
    public String transactionType; // Either "IN" or "OUT"

    @Column(precision = 19, scale = 2)
    public BigDecimal quantityChanged; // Quantity added or removed in this transaction

    @Column(precision = 19, scale = 2)
    public BigDecimal stockAfterTransaction; // Stock quantity after transaction

    /** {@link org.example.inventory.stock.domains.StockBatch} id for this store batch. */
    @Column
    public Long stockBatchId;

    @Column
    public Long storeId;

    /**
     * What caused the movement, e.g. STOCK_RECEIVE, STOCK_TRANSFER_OUT, TREATMENT_CHART_DISPENSE.
     */
    @Column(length = 80)
    public String sourceEvent;

    /** Id of the related entity (StockReceive, StockTransfer, TreatmentChart, …). */
    @Column
    public Long referenceId;

    /** Type of {@link #referenceId}, e.g. StockReceive, StockTransfer, TreatmentChart. */
    @Column(length = 80)
    public String referenceType;

    @PrePersist
    public void prePersist() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
