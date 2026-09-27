package org.example.visit.services.paloads.requests;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class PatientVisitRequest {

    @Schema(example = "outpatient")
    public String visitType;

    @Schema(example = "Review")
    public String visitReason;

    @Schema(example = "2026-06-19")
    @JsonbDateFormat(value = "yyyy-MM-dd")
    public LocalDate visitDate;

    @Schema(example = "09:30")
    @JsonbDateFormat(value = "HH:mm")
    public LocalTime visitTime;

    @Schema(example = "28")
    public BigDecimal patientAge;

    @Schema(example = "Bugogo")
    public String patientAddress;

    @Schema(example = "256784411848")
    public String patientContact;

    @Schema(example = "peasant")
    public String occupation;

    @Schema(example = "Tumwesigye Benjamin")
    public String nextOfKinName;

    @Schema(example = "256702225307")
    public String nextOfKinContact;

    @Schema(example = "Brother")
    public String relationship;

    @Schema(example = "Kampala")
    public String nextOfKinAddress;

    /**
     * When true, allow creating another open visit even if the patient already has one.
     * Used for over-the-counter pharmacy debt visits.
     */
    @Schema(example = "false")
    public Boolean allowConcurrentOpen;
}
