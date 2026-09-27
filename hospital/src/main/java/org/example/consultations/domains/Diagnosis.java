package org.example.consultations.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.example.treatment.domains.TreatmentRequested;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Diagnosis extends PanacheEntity {

    /** Clinical diagnosis name, e.g. "Pneumonia". */
    @Column(columnDefinition = "TEXT", nullable = false)
    public String name;

    /** Uganda HMIS line code, e.g. MA., EP01, DY. */
    @Column(length = 20)
    public String hmisCode;

    /** Optional ICD-10 code for reporting / future use. */
    @Column(length = 20)
    public String icd10Code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_type_id")
    public DiagnosisType diagnosisType;

    /** Clinical severity, e.g. "Mild", "Moderate", "Severe". */
    @Column(length = 80)
    public String severity;

    /** Optional: "final" or "differential". */
    @Column(length = 40)
    public String kind;

    @Column(columnDefinition = "TEXT")
    public String notes;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    public Consultation consultation;

    /**
     * Treatments prescribed for this diagnosis.
     * Visit FK on TreatmentRequested remains the billing root; this is clinical linkage only.
     */
    @OneToMany(mappedBy = "diagnosis", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    public List<TreatmentRequested> treatments = new ArrayList<>();

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;
}
