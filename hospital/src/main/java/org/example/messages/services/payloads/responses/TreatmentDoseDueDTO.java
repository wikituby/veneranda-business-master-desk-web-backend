package org.example.messages.services.payloads.responses;

import org.example.treatment.treatmentChart.domains.TreatmentChart;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class TreatmentDoseDueDTO {
    public Long chartId;
    public Long treatmentRequestedId;
    public Long visitId;
    public Long patientId;
    public String patientName;
    public String treatmentName;
    public String nextDoseDate;
    public String nextDoseTime;
    public Integer totalDosagesRemaining;
    public String dosage;
    public String route;

    public TreatmentDoseDueDTO() {
    }

    public TreatmentDoseDueDTO(TreatmentChart chart) {
        if (chart == null) {
            return;
        }
        this.chartId = chart.id;
        if (chart.treatmentRequested != null) {
            this.treatmentRequestedId = chart.treatmentRequested.id;
            this.treatmentName = chart.treatmentRequested.itemName;
            if (chart.treatmentRequested.visit != null) {
                this.visitId = chart.treatmentRequested.visit.id;
                this.patientName = chart.treatmentRequested.visit.patientName;
                if (chart.treatmentRequested.visit.patient != null) {
                    this.patientId = chart.treatmentRequested.visit.patient.id;
                }
            }
        }
        if (chart.dateForNextDosage != null) {
            this.nextDoseDate = chart.dateForNextDosage.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (chart.timeForNextDosage != null) {
            this.nextDoseTime = chart.timeForNextDosage.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        this.totalDosagesRemaining = chart.totalDosagesRemaining;
        this.dosage = formatDosage(chart.dosageValue, chart.dosageUnit);
        this.route = chart.route;
    }

    private static String formatDosage(BigDecimal value, String unit) {
        if (value == null && (unit == null || unit.isBlank())) {
            return null;
        }
        String amount = value != null ? value.stripTrailingZeros().toPlainString() : "";
        String u = unit != null ? unit.trim() : "";
        return (amount + " " + u).trim();
    }
}
