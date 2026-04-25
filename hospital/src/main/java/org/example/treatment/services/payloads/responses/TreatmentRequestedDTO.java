package org.example.treatment.services.payloads.responses;
import jakarta.persistence.Column;
import org.example.treatment.domains.TreatmentRequested;

import java.math.BigDecimal;

public class TreatmentRequestedDTO {
    public Long id;
    public BigDecimal quantity;
    public BigDecimal unitSellingPrice;
    public BigDecimal totalAmount;
    public BigDecimal unitCostPrice;
    public BigDecimal provisionalTotalAmount;
    public String itemName;
    public Integer shelfNumber;
    public BigDecimal provisionalQuantity;
    public BigDecimal totalUnits;

    public BigDecimal availableQuantity;

    public BigDecimal lastUpDateQuantity;

    public BigDecimal lastStockAtHand;

    public BigDecimal amountPerFrequencyValue;

    public String amountPerFrequencyUnit;

    public BigDecimal frequencyValue;

    public String frequencyUnit;

    public BigDecimal durationValue;

    public BigDecimal lastUnitValue;

    public String durationUnit;

    public Long itemId;

    public BigDecimal unitBuy;


    public TreatmentRequestedDTO(TreatmentRequested treatmentRequested) {
        this.id = treatmentRequested.id;
        this.itemId = treatmentRequested.itemId;

        this.lastStockAtHand = treatmentRequested.lastStockAtHand;
        this.amountPerFrequencyValue = treatmentRequested.amountPerFrequencyValue;
        this.amountPerFrequencyUnit = treatmentRequested.amountPerFrequencyUnit;

        this.frequencyValue = treatmentRequested.frequencyValue;
        this.durationValue = treatmentRequested.durationValue;
        this.frequencyUnit = treatmentRequested.frequencyUnit;

        this.lastUnitValue = treatmentRequested.lastUnitValue;
        this.totalUnits = treatmentRequested.totalUnits;


        this.durationUnit = treatmentRequested.durationUnit;


        this.lastUpDateQuantity = treatmentRequested.lastUpDateQuantity;
        this.quantity = treatmentRequested.quantity;
        this.shelfNumber = treatmentRequested.getShelfNumber(); // Use getter method
        this.availableQuantity = treatmentRequested.availableQuantity;
        this.unitBuy = treatmentRequested.unitBuy;

        this.provisionalQuantity = treatmentRequested.provisionalQuantity;
        this.unitSellingPrice = treatmentRequested.unitSellingPrice;
        this.provisionalTotalAmount = treatmentRequested.provisionalTotalAmount;
        this.totalAmount = treatmentRequested.totalAmount;
        this.unitCostPrice = treatmentRequested.unitBuy;
        this.itemName = treatmentRequested.itemName;

    }
}





