package org.example.vitals.services.payloads.requests;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


public class VitalsMonitoringChartRequest {
    // Foreign key to link vitals monitoring with a specific patient visit
    @Schema(example = "1")
    public Long visitId;

    @Schema(example = "1")
    public Long userId;// The visit the vitals belong to

    // Blood Pressure (e.g., 120/80)
    @Schema(example = "120/80")
    public String bloodPressure; // Blood Pressure in "Systolic/Diastolic" format

    // Mean Arterial Pressure (MAP) calculation (e.g., 70 mmHg)
    @Schema(example = "70")
    public Double map;  // MAP in mmHg

    // Oxygen Saturation (SpO2) level (e.g., 98%)
    @Schema(example = "98")
    public Double spO2;  // Oxygen saturation in percentage

    // Pulse Rate (e.g., 75 bpm)
    @Schema(example = "75")
    public Integer pulseRate;  // Pulse rate in beats per minute (bpm)

    // Body Temperature (e.g., 37.2°C)
    @Schema(example = "37.2")
    public Double temperature;  // Temperature in Celsius

    // Respiratory Rate (e.g., 18 breaths per minute)
    @Schema(example = "18")
    public Integer respiratoryRate;  // Respiratory rate in breaths per minute

}






