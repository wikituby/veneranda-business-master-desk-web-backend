package org.example.inventory.stock.services.payloads.requests;

import java.math.BigDecimal;

public class StockDosageRuleRequest {
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
}
