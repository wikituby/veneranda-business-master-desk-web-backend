package org.example.diagnostics.ultrasoundScan.generalUs.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.treatment.domains.TreatmentRequested;
import org.example.user.domains.User;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class GeneralUs extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "visit_id", nullable = false)
    public PatientVisit visit;

    @OneToOne
    @JoinColumn(name = "procedureRequested_id", nullable = false)
    public ProcedureRequested procedureRequested;

    @Column(columnDefinition = "TEXT")
    public String indication;

    @Column
    public String patientName;

    @Column
    public String scanReportTitle;

    @Column
    public BigDecimal patientAge;

    @Column
    public String gender;

    @Column
    public String doneBy;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User performer;

    @Column(columnDefinition = "TEXT")
    public String exam;

    @Column(columnDefinition = "TEXT")
    public String findings;

    @Column(columnDefinition = "TEXT")
    public String impression;

    @Column(columnDefinition = "TEXT")
    public String recommendation;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate upDatedDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate scanRequestDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate scanPerformingDate;

    @Column
    public LocalTime timeOfProcedure;
}
