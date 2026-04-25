package org.example.vitals.services.payloads.responses;

import org.example.vitals.domains.VitalsMonitoringChart;

import java.time.LocalDateTime;

public class VitalsMonitoringChartDTO {
    public Long id;
    public Long visitId;  // The ID of the associated patient visit
    public LocalDateTime dateTime;  // The date and time when vitals were monitored
    public String bloodPressure;  // Blood Pressure in "Systolic/Diastolic" format
    public Double map;  // Mean Arterial Pressure (MAP) in mmHg
    public Double spO2;  // Oxygen saturation percentage
    public Integer pulseRate;  // Pulse rate in beats per minute (bpm)
    public Double temperature;  // Temperature in Celsius
    public Integer respiratoryRate;  // Respiratory rate in breaths per minute
    public String initials;  // Initials of the healthcare professional

    public VitalsMonitoringChartDTO(VitalsMonitoringChart vitalsMonitoringChart) {
        this.id = vitalsMonitoringChart.id;
        this.visitId = vitalsMonitoringChart.visit != null ? vitalsMonitoringChart.visit.id : null;
        this.dateTime = vitalsMonitoringChart.dateTime;
        this.bloodPressure = vitalsMonitoringChart.bloodPressure;
        this.map = vitalsMonitoringChart.map;
        this.spO2 = vitalsMonitoringChart.spO2;
        this.pulseRate = vitalsMonitoringChart.pulseRate;
        this.temperature = vitalsMonitoringChart.temperature;
        this.respiratoryRate = vitalsMonitoringChart.respiratoryRate;
        this.initials = vitalsMonitoringChart.initials;
    }
}






