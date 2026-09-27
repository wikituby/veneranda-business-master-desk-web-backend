package org.example.pharmacy.otc.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.example.inventory.stock.domains.StockBatch;

import java.math.BigDecimal;

/**
 * One product line on an OTC sale.
 * Prefer {@link StockBatch}; legacy shop {@code itemId} is allowed when no batch is used.
 */
@Entity
@Table(name = "otc_pharmacy_sale_line")
public class OtcPharmacySaleLine extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    public OtcPharmacySale sale;

    /** Nullable — shop-item (ITM) OTC lines have no stock batch. */
    @ManyToOne
    @JoinColumn(name = "stock_batch_id", nullable = true)
    public StockBatch stockBatch;

    /** Legacy shop item id when the line was sold without a stock batch. */
    @Column(name = "item_id")
    public Long itemId;

    @Column
    public Long stockItemId;

    @Column(nullable = false)
    public String itemName;

    @Column
    public String batchNumber;

    @Column
    public Long storeId;

    @Column
    public String storeName;

    @Column(nullable = false)
    public BigDecimal quantity;

    @Column(nullable = false)
    public BigDecimal unitBuy;

    @Column(nullable = false)
    public BigDecimal unitSellingPrice;

    @Column(nullable = false)
    public BigDecimal totalAmount;

    /** Batch/item quantity remaining after this line was sold. */
    @Column
    public BigDecimal stockAtHandAfter;

    @Column
    public BigDecimal amountPerFrequencyValue;

    @Column(length = 64)
    public String amountPerFrequencyUnit;

    @Column
    public BigDecimal frequencyValue;

    /** 1 = day, 7 = week, 30 = month (same as patient visit pharmacy). */
    @Column
    public Integer frequencyUnit;

    @Column
    public BigDecimal durationValue;

    @Column
    public Integer durationUnit;

    @Column
    public BigDecimal totalUnits;

    @Column(length = 255)
    public String instructions;

    @Column(length = 128)
    public String route;

    /** When true, stock was not deducted at sale time (backorder); checked at delivery. */
    @Column(name = "stockdeferred", nullable = false)
    public boolean stockDeferred = false;

    /** When true, stock was restored and the line is kept for history. */
    @Column(nullable = false)
    public boolean reversed = false;

    /** Linked prescription line when this sale came from a doctor Rx dispense. */
    @Column(name = "treatment_requested_id")
    public Long treatmentRequestedId;

    /** Linked visit sundry when this sale line came from pharmacy sundry dispense. */
    @Column(name = "visit_sundry_id")
    public Long visitSundryId;
}
