package org.example.vitals.services.payloads.requests;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class InitialVitalUpdateRequest {

    // Blood Pressure (BP) in mmHg (e.g., 120/80)
    @Schema(example = "120")
    public Double systolic;  // Blood Pressure in "Systolic/Diastolic" format

    @Schema(example = "90")
    public Double diastolic;  // Blood Pressure in "Systolic/Diastolic" format

    // Body Temperature in Celsius (e.g., 37.2°C)
    @Schema(example = "36")
    public Double temperature;  // Temperature in Celsius

    @Schema(example = "Triage")
    public String station;

    @Schema(example = "Triage")
    public String takenBy;

    // Weight of the patient in kilograms (e.g., 70 kg)
    @Schema(example = "70")
    public Double weight;  // Weight in kilograms (kg)

    // Height of the patient in centimeters (e.g., 175 cm)
    @Schema(example = "5.8")
    public Double height;  // Height in centimeters (cm)

    @Schema(example = "23.5")
    public BigDecimal muac;  // Mid-upper arm circumference (cm)

    // Oxygen Saturation (SpO2) level (e.g., 98%)
    @Schema(example = "70")
    public Double spO2;  // Oxygen saturation in percentage

    // Heart Rate (HR) in beats per minute (e.g., 75 bpm)
    @Schema(example = "75")
    public Integer heartRate;  // Heart rate in beats per minute (bpm)

    @Schema(example = "75")
    public Double pulseRate;  // Heart rate in beats per minute (bpm)

    // Respiratory Rate (RR) in breaths per minute (e.g., 18 breaths/min)
    @Schema(example = "18")
    public Integer respiratoryRate;  // Respiratory rate in breaths per minute

    @Schema(example = "1994/02/24")
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateTaken;  // Matches the "dd/MM/yyyy" format in JSON

    public LocalTime timeTaken;
}






