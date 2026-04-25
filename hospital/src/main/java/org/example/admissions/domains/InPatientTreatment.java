package org.example.admissions.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class InPatientTreatment extends PanacheEntity {

    @Column(nullable = false)
    public LocalDate dateGiven;

    @Column(nullable = false)
    public LocalTime timeGiven;

    @Column(nullable = false)
    public String medicine;

    @Column(nullable = false)
    public String dose;

    @Column(nullable = false)
    public String frequency;

    @Column(nullable = false)
    public String route;

    @Column(nullable = false)
    public String administeredByInitials;

    @ManyToOne
    @JoinColumn(nullable = false)
    public PatientVisit visit;
}






