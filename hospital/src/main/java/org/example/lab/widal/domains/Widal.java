package org.example.lab.widal.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;

import org.example.treatment.domains.TreatmentRequested;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDate;

@Entity
public class Widal extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "visit_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "FK_widal_visit"))
    public PatientVisit visit;

    @OneToOne
    @JoinColumn(name = "treatmentRequested_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "FK_widal_treatment"))
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
