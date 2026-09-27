package org.example.treatment.treatmentChart.services.payloads.responses;

import org.example.treatment.treatmentChart.domains.TreatmentChart;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class TreatmentChartDTO {
    public Long id;
    public Long treatmentRequestedId;
    public String treatmentName;
    public LocalDate dateGiven;
    public LocalTime timeGiven;
    public String timeBetweenGivenToNextDosage;
    public LocalDate dateForNextDosage;
    public LocalTime timeForNextDosage;
    public String givenBy;
    public String route;
    public String instructions;
    public BigDecimal dosageValue;
    public String dosageUnit;
    public BigDecimal frequencyValue;
    public String frequencyUnit;
    public String frequency;
    public Integer overallTotalDosages;
    public Integer totalDosagesGiven;
    public Integer unitsUsed;
    public Integer totalDosagesRemaining;
    public String status;
    public String administrationPlace;

    public TreatmentChartDTO(TreatmentChart chart) {
        if (chart != null) {
            this.id = chart.id;
            this.treatmentRequestedId = chart.treatmentRequested != null ? chart.treatmentRequested.id : null;
            this.treatmentName = chart.treatmentRequested != null ? chart.treatmentRequested.itemName : null;
            this.dateGiven = chart.dateGiven;
            this.timeGiven = chart.timeGiven;
            this.timeBetweenGivenToNextDosage = chart.timeBetweenGivenToNextDosage;
            this.dateForNextDosage = chart.dateForNextDosage;
            this.timeForNextDosage = chart.timeForNextDosage;
            this.givenBy = chart.givenBy;
            this.route = chart.route;
            this.instructions = chart.instructions;
            this.dosageValue = chart.dosageValue;
            this.dosageUnit = chart.dosageUnit;
            this.frequencyValue = chart.frequencyValue;
            this.frequencyUnit = chart.frequencyUnit;
            this.frequency = buildFrequencyString(chart.frequencyValue, chart.frequencyUnit);
            this.overallTotalDosages = chart.overallTotalDosages;
            this.totalDosagesGiven = chart.totalDosagesGiven;
            this.unitsUsed = chart.unitsUsed;
            this.totalDosagesRemaining = chart.totalDosagesRemaining;
            this.status = chart.status;
            this.administrationPlace = chart.administrationPlace;
        }
    }

    private String buildFrequencyString(BigDecimal frequencyValue, String frequencyUnit) {
        if (frequencyValue == null && frequencyUnit == null) {
            return null;
        }
        String value = frequencyValue != null
                ? frequencyValue.stripTrailingZeros().toPlainString()
                : "";
        String unit = frequencyUnit != null ? frequencyUnit : "";
        String space = value.isEmpty() || unit.isEmpty() ? "" : " ";
        return value + space + unit;
    }
}





















