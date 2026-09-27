package org.example.treatment.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.example.consultations.domains.Diagnosis;
import org.example.inventory.stock.domains.StockBatch;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;

@Entity
@Table(name = "TreatmentRequested")
public class TreatmentRequested extends PanacheEntity {

    // Reference to the associated visit
    @ManyToOne
    @JoinColumn(nullable = false)
    public PatientVisit visit;

    /** Optional clinical link: treatment prescribed for a specific diagnosis. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id")
    public Diagnosis diagnosis;

    // Quantity of lab tests requested
    @Column(nullable = false)
    public BigDecimal quantity;

    @Column(nullable = false)
    public BigDecimal provisionalQuantity;

    @Column
    public BigDecimal amountPerFrequencyValue;

    @Column
    public String amountPerFrequencyUnit;

    @Column
    public BigDecimal frequencyValue;

    @Column
    public String frequencyUnit;

    @Column
    public BigDecimal durationValue;

    @Column
    public String durationUnit;

    @Column
    public BigDecimal lastUnitValue;

    @Column
    public BigDecimal totalUnits;

    @Column
    public String instructions;

    @Column
    public String route;

    // Unit price
    @Column(nullable = false)
    public BigDecimal unitSellingPrice;

    // Total amount
    @Column(nullable = false)
    public BigDecimal totalAmount;


    @Column
    public Integer shelfNumber;

    @Column(nullable = false)
    public BigDecimal provisionalTotalAmount;

    // Item details
    @Column(nullable = false)
    public String itemName;

    @Column
    public BigDecimal lastStockAtHand;

    @Column
    public BigDecimal unitBuy;

    @Column
    public BigDecimal availableQuantity;

    @Column
    public BigDecimal lastUpDateQuantity;

    @Column(nullable = false)
    public Long itemId;

    @ManyToOne
    @JoinColumn(name = "stock_batch_id")
    public StockBatch stockBatch;

    @Column
    public Long unitSellingModelId;

    @Column
    public String status;

    /** PD: counted toward visit invoice / TT Sell. Null treated as paid for legacy rows. */
    @Column
    public Boolean paid;

    /** DSP: picked / dispensed from pharmacy. */
    @Column
    public Boolean dispensed;

    /** ADM: administered / all chart doses given. */
    @Column
    public Boolean administered;

    /** Visit invoice includes this line when paid (default) and not cancelled. */
    public boolean countsTowardInvoice() {
        if (status != null && "cancelled".equalsIgnoreCase(status.trim())) {
            return false;
        }
        return paid == null || Boolean.TRUE.equals(paid);
    }

    public boolean isDispensedOrGiven() {
        if (Boolean.TRUE.equals(dispensed)) {
            return true;
        }
        if (status == null) {
            return false;
        }
        String s = status.trim().toLowerCase();
        return "given".equals(s) || "dispensed".equals(s);
    }

    // =========================
    // ONLY REQUIRED METHODS
    // =========================

    public Integer getShelfNumber() {
        return shelfNumber;
    }

    public void setShelfNumber(Integer shelfNumber) {
        this.shelfNumber = shelfNumber;
    }
}




