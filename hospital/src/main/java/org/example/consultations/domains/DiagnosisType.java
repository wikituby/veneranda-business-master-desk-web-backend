package org.example.consultations.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class DiagnosisType extends PanacheEntity {

    @Column(nullable = false)
    public String title;

    /** Uganda HMIS code for this diagnosis catalog entry. */
    @Column(length = 20)
    public String hmisCode;

    @Column(length = 20)
    public String icd10Code;

    /** Comma-separated keywords for auto-mapping free-text diagnoses (e.g. malaria,fever). */
    @Column(columnDefinition = "TEXT")
    public String matchKeywords;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;
}
