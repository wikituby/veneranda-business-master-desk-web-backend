package org.example.lab.multipleParameterReport.cbc.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;

import org.example.treatment.domains.TreatmentRequested;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDate;

@Entity
public class Cbc extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "visit_id", nullable = false)
    public PatientVisit visit;

    @OneToOne
    @JoinColumn(name = "treatmentRequested_id", nullable = false)
    public TreatmentRequested treatmentRequested;

    @Column
    public String result;

    @Column
    public String notes;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public String upDatedDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;
}
