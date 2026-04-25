package org.example.lab.parasitologyStool.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "LabParasitologyStoolReport")
public class ParasitologyStool extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "visit_id", nullable = false)
    public PatientVisit visit;

    @OneToOne
    @JoinColumn(name = "procedureRequested_id", nullable = false)
    public ProcedureRequested procedureRequested;

    @Column
    public String test;

    @Column
    public String patientName;

    @Column
    public String gender;

    @Column
    public BigDecimal patientAge;

    @Column
    public String labReportTitle;

    @Column
    public String doneBy;

    @Column
    public String ova;

    @Column
    public String cysts;

    @Column
    public String larvae;

    @Column
    public String color;

    @Column
    public String consistency;

    @Column
    public String blood;

    @Column
    public String mucus;

    @Column
    public String visibleParasites;

    @Column
    public String others;

    @Column
    public String interpretations;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDateTime sampleCollectionDateAndTime;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDateTime procedureDoneDateAndTime;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDateTime reportUpDatedDateAndTime;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDateTime reportCreationDateAndTime;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate labRequestDate;
}
























