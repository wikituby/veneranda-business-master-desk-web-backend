package org.example.consultations.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

/**
 * Structured presenting complaint (PC) for a consultation.
 * Clinicians can add multiple rows with SOCRATES-style detail.
 */
@Entity
public class PresentingComplaint extends PanacheEntity {

    /** The presenting complaint itself, e.g. "Chest pain". */
    @Column(columnDefinition = "TEXT", nullable = false)
    public String complaint;

    /** Anatomical site, e.g. "Left lower chest". */
    @Column(columnDefinition = "TEXT")
    public String site;

    /** Mild / Moderate / Severe (or free text). */
    @Column(columnDefinition = "TEXT")
    public String severity;

    /** Sudden / Gradual (or free text). */
    @Column(columnDefinition = "TEXT")
    public String onset;

    /** Numeric duration amount, e.g. 3. */
    @Column
    public Integer durationValue;

    /** day | week | month | year */
    @Column(length = 20)
    public String durationUnit;

    /**
     * Legacy/display duration text (e.g. "3 day(s)").
     * Kept in sync from durationValue + durationUnit.
     */
    @Column(columnDefinition = "TEXT")
    public String duration;

    /** Nature / character, e.g. "Sharp", "Dull", "Throbbing". */
    @Column(columnDefinition = "TEXT")
    public String nature;

    /** Course / progression, e.g. "Worsening", "Improving", "Stable". */
    @Column(columnDefinition = "TEXT")
    public String course;

    @Column(columnDefinition = "TEXT")
    public String aggravatingFactors;

    @Column(columnDefinition = "TEXT")
    public String alleviatingFactors;

    @Column(columnDefinition = "TEXT")
    public String associatedSymptoms;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    public Consultation consultation;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;
}
