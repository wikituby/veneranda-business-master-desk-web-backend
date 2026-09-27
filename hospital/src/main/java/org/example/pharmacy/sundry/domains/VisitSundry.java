package org.example.pharmacy.sundry.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class VisitSundry extends PanacheEntity {

    @Column(name = "patient_visit_id", nullable = false)
    public Long patientVisitId;

    /** Set when sundries are recorded against a pharmacy stock batch. */
    @Column(name = "stock_batch_id")
    public Long stockBatchId;

    /** Set when sundries are recorded against a shop/stock item (non-batch mode). */
    @Column(name = "item_id")
    public Long itemId;

    @Column
    public String itemName;

    @Column
    public String unitOfMeasure;

    @Column(nullable = false)
    public BigDecimal quantityUsed;

    @Column
    public BigDecimal unitSellingPrice;

    @Column
    public BigDecimal unitCostPrice;

    @Column
    public BigDecimal lineTotal;

    @Column
    public String usedBy;

    @Column
    public LocalDateTime recordedAt;

    /**
     * Set when pharmacy includes this sundry on a dispense/sale.
     * Stock was already deducted when the sundry was recorded.
     */
    @Column
    public Boolean dispensed;
}
