package org.example.vitals.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDateTime;

@Entity
public class VitalsMonitoringChart extends PanacheEntity {

    // Foreign key to link vitals monitoring with a specific patient visit
    @ManyToOne
    @JoinColumn(
            name = "visit_id",
            foreignKey = @ForeignKey(name = "fk_vitals_monitoring_chart_visit")
    )
    public PatientVisit visit;

    // The date and time when the vitals were monitored
    @Column(nullable = false)
    public LocalDateTime dateTime;

    // Blood Pressure (e.g., 120/80)
    @Column(nullable = false)
    public String bloodPressure; // Blood Pressure in "Systolic/Diastolic" format

    // Mean Arterial Pressure (MAP) calculation (e.g., 70 mmHg)
    @Column(nullable = false)
    public Double map;  // MAP in mmHg

    // Oxygen Saturation (SpO2) level (e.g., 98%)
    @Column(nullable = false)
    public Double spO2;  // Oxygen saturation in percentage

    // Pulse Rate (e.g., 75 bpm)
    @Column(nullable = false)
    public Integer pulseRate;  // Pulse rate in beats per minute (bpm)

    // Body Temperature (e.g., 37.2°C)
    @Column(nullable = false)
    public Double temperature;  // Temperature in Celsius

    // Respiratory Rate (e.g., 18 breaths per minute)
    @Column(nullable = false)
    public Integer respiratoryRate;  // Respiratory rate in breaths per minute

    // Initials of the healthcare provider who took the vitals
    @Column(nullable = false)
    public String initials;  // Initials of the healthcare professional monitoring vitals



}






