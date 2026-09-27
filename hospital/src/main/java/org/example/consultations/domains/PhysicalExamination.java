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
 * Structured physical / clinical examination finding for a consultation.
 * Clinicians can add multiple system-specific exam rows.
 */
@Entity
public class PhysicalExamination extends PanacheEntity {

    /** Examined system/region, e.g. "Respiratory", "Cardiovascular". */
    @Column(name = "exam_system", columnDefinition = "TEXT", nullable = false)
    public String examSystem;

    /** Main examination findings. */
    @Column(columnDefinition = "TEXT", nullable = false)
    public String findings;

    /** Optional anatomical site / laterality. */
    @Column(columnDefinition = "TEXT")
    public String site;

    /** Normal / Abnormal (or free text). */
    @Column(columnDefinition = "TEXT")
    public String status;

    @Column(columnDefinition = "TEXT")
    public String inspection;

    @Column(columnDefinition = "TEXT")
    public String palpation;

    @Column(columnDefinition = "TEXT")
    public String percussion;

    @Column(columnDefinition = "TEXT")
    public String auscultation;

    @Column(columnDefinition = "TEXT")
    public String notes;

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
