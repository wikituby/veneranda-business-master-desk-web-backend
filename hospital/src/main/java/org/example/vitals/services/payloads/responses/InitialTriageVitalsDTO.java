package org.example.vitals.services.payloads.responses;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.example.vitals.domains.InitialTriageVitals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class InitialTriageVitalsDTO {
    public Long id;
    public LocalDate dateTaken;
    public LocalTime timeTaken;
    public Double systolic;
    public Double diastolic;
    public Double map;
    public Double pulsePressure;
    public String bloodPressure;
    public String takenBy;
    public Double pulseRate;
    public Double temperature;
    public Double weight;
    public Double height;

    @Schema(description = "Body mass index (kg/m²). Computed from weight (kg) and height (cm); persisted on save and recomputed here if missing.", example = "22.49")
    public BigDecimal bmi;

    public BigDecimal muac;
    public String station;
    public Double spO2;
    public Integer heartRate;
    public Integer respiratoryRate;
    public Long visitId;

    public InitialTriageVitalsDTO(InitialTriageVitals initialTriageVitals) {
        if (initialTriageVitals == null) {
            return;
        }
        this.id = initialTriageVitals.id;
        this.dateTaken = initialTriageVitals.dateTaken;
        this.takenBy = initialTriageVitals.takenBy;
        this.pulseRate = initialTriageVitals.pulseRate;
        this.station = initialTriageVitals.station;
        this.timeTaken = initialTriageVitals.timeTaken;
        this.systolic = initialTriageVitals.systolic;
        this.diastolic = initialTriageVitals.diastolic;
        this.bloodPressure = initialTriageVitals.bloodPressure;
        this.map = initialTriageVitals.map;
        this.pulsePressure = initialTriageVitals.pulsePressure != null
                ? initialTriageVitals.pulsePressure
                : (initialTriageVitals.systolic != null && initialTriageVitals.diastolic != null
                    ? initialTriageVitals.systolic - initialTriageVitals.diastolic
                    : null);
        this.temperature = initialTriageVitals.temperature;
        this.weight = initialTriageVitals.weight;
        this.height = initialTriageVitals.height;
        this.bmi = initialTriageVitals.bmi != null
                ? initialTriageVitals.bmi
                : InitialTriageVitals.computeBmi(initialTriageVitals.weight, initialTriageVitals.height);
        this.muac = initialTriageVitals.muac;
        this.spO2 = initialTriageVitals.spO2;
        this.heartRate = initialTriageVitals.heartRate;
        this.respiratoryRate = initialTriageVitals.respiratoryRate;
        this.visitId = initialTriageVitals.visit != null ? initialTriageVitals.visit.id : null;
    }
}






