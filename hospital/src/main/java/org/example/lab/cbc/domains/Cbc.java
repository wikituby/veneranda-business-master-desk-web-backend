package org.example.lab.cbc.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.visit.domains.PatientVisit;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "LabCbcReport")
@DynamicUpdate
public class Cbc extends PanacheEntity {

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
    public String wbc;

    @Column
    public String lymph;

    @Column
    public String mid;

    @Column
    public String gran;

    @Column
    public String lymphPercent;

    @Column
    public String midPercent;

    @Column
    public String granPercent;

    @Column
    public String hgb;

    @Column
    public String rbc;

    @Column
    public String hct;

    @Column
    public String mcv;

    @Column
    public String mch;

    @Column
    public String mchc;

    @Column
    public String rdwCv;

    @Column
    public String rdwSd;

    @Column
    public String plt;

    @Column
    public String mpv;

    @Column
    public String pdw;

    @Column
    public String pct;

    /** Stored as JSON string; LONGTEXT avoids MySQL truncation (VARCHAR/TEXT limits). */
    @Column(columnDefinition = "TEXT")
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
























