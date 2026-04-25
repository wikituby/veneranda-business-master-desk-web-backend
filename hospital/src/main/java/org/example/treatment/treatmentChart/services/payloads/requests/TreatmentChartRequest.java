package org.example.treatment.treatmentChart.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class TreatmentChartRequest {
    @Schema(example = "Nurse Jane")
    public String givenBy;

    @Schema(example = "Oral")
    public String route;

    @Schema(example = "500")
    public BigDecimal dosageValue;

    @Schema(example = "mg")
    public String dosageUnit;

    @Schema(example = "2")
    public BigDecimal frequencyValue;

    @Schema(example = "per Day")
    public String frequencyUnit;

    @Schema(example = "2024-01-15")
    public LocalDate dateGiven;

    @Schema(example = "10:30:00")
    public LocalTime timeGiven;

    @Schema(example = "Given")
    public String status;

    @Schema(example = "1")
    public Integer totalDosagesGiven;

    @Schema(example = "02:00:00")
    public LocalTime timeBetweenGivenToNextDosage;

    @Schema(example = "2024-01-15")
    public LocalDate dateForNextDosage;

    @Schema(example = "10:00:00")
    public LocalTime timeForNextDosage;

    @Schema(example = "5")
    public Integer unitsUsed;
}





















