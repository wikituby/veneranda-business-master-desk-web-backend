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
public class StockAdjustment extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "stock_batch_id", nullable = false)
    public StockBatch stockBatch;

    @ManyToOne(optional = false)
    @JoinColumn(name = "adjustment_type_id", nullable = false)
    public AdjustmentType adjustmentType;

    /** Snapshot before adjustment (matches {@link StockBatch#stockAtHand} before apply). */
    @Column(precision = 19, scale = 2)
    public BigDecimal quantityBefore;

    /** Signed delta applied to batch (positive increases stock, negative decreases). */
    @Column(precision = 19, scale = 2)
    public BigDecimal quantityChanged;

    @Column(precision = 19, scale = 2)
    public BigDecimal quantityAfter;

    @Column(length = 500)
    public String reason;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime date;

    @Column(length = 160)
    public String doneBy;
}
