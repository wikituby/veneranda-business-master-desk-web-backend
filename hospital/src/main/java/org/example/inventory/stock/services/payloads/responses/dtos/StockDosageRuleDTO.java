package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.StockDosageRule;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockDosageRuleDTO {
    public Long id;
    public Long stockItemId;
    public String stockItemName;
    public String title;
    public String matchLabel;
    public String ruleLabel;
    public String doseCalculationType;
    public BigDecimal fixedDoseValue;
    public String fixedDoseUnit;
    public BigDecimal weightDoseValue;
    public String weightDoseUnit;
    public BigDecimal maxDoseValue;
    public String maxDoseUnit;
    public BigDecimal minAgeYears;
    public BigDecimal maxAgeYears;
    public BigDecimal minWeightKg;
    public BigDecimal maxWeightKg;
    public String route;
    public BigDecimal frequencyValue;
    public BigDecimal frequencyUnit;
    public BigDecimal durationValue;
    public BigDecimal durationUnit;
    public BigDecimal quantity;
    public String quantityUnit;
    public BigDecimal stockUnitsPerDose;
    public String stockUnitLabel;
    public String instructions;
    public String specialScheduleText;
    public String warning;
    public Boolean active;
    public Integer sortOrder;
    public LocalDate creationDate;
    public LocalDate updateDate;

    public StockDosageRuleDTO(StockDosageRule entity) {
        this.id = entity.id;
        this.stockItemId = entity.stockItem != null ? entity.stockItem.id : null;
        this.stockItemName = entity.stockItem != null ? entity.stockItem.stockItemName : null;
        this.title = entity.title;
        this.matchLabel = entity.matchLabel;
        this.ruleLabel = entity.ruleLabel;
        this.doseCalculationType = entity.doseCalculationType;
        this.fixedDoseValue = entity.fixedDoseValue;
        this.fixedDoseUnit = entity.fixedDoseUnit;
        this.weightDoseValue = entity.weightDoseValue;
        this.weightDoseUnit = entity.weightDoseUnit;
        this.maxDoseValue = entity.maxDoseValue;
        this.maxDoseUnit = entity.maxDoseUnit;
        this.minAgeYears = entity.minAgeYears;
        this.maxAgeYears = entity.maxAgeYears;
        this.minWeightKg = entity.minWeightKg;
        this.maxWeightKg = entity.maxWeightKg;
        this.route = entity.route;
        this.frequencyValue = entity.frequencyValue;
        this.frequencyUnit = entity.frequencyUnit;
        this.durationValue = entity.durationValue;
        this.durationUnit = entity.durationUnit;
        this.quantity = entity.quantity;
        this.quantityUnit = entity.quantityUnit;
        this.stockUnitsPerDose = entity.stockUnitsPerDose;
        this.stockUnitLabel = entity.stockUnitLabel;
        this.instructions = entity.instructions;
        this.specialScheduleText = entity.specialScheduleText;
        this.warning = entity.warning;
        this.active = entity.active;
        this.sortOrder = entity.sortOrder;
        this.creationDate = entity.creationDate;
        this.updateDate = entity.updateDate;
    }
}
