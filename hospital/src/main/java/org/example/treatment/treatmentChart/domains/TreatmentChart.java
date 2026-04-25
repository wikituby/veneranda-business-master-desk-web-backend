package org.example.treatment.treatmentChart.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.example.treatment.domains.TreatmentRequested;

@Entity
public class TreatmentChart extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "treatmentRequested_id", nullable = false)
    public TreatmentRequested treatmentRequested;

    @Column
    public LocalDate dateGiven;

    @Column
    public LocalTime timeGiven;

    @Column
    public LocalTime timeBetweenGivenToNextDosage;

    @Column
    public LocalDate dateForNextDosage;

    @Column
    public LocalTime timeForNextDosage;

    @Column
    public String givenBy;

    @Column
    public String route;

    @Column
    public BigDecimal dosageValue;

    @Column
    public String dosageUnit;

    @Column
    public BigDecimal frequencyValue;

    @Column
    public String frequencyUnit;

    @Column
    public Integer overallTotalDosages;

    @Column
    public Integer totalDosagesGiven;

    @Column
    public Integer unitsUsed;

    @Column
    public Integer totalDosagesRemaining;

    @Column
    public String status;



}










