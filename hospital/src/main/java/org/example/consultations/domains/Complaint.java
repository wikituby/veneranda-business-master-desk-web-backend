package org.example.consultations.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

@Entity
public class Complaint extends PanacheEntity {

    // Site on the body where the complaint is located
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    public ComplaintSite site;

    // Type of complaint (e.g., "Pain", "Discomfort", "Swelling")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    public ComplaintType type;

    // Duration of the complaint (e.g., "3 days", "2 weeks")
    @Column(columnDefinition = "TEXT")
    public String duration;

    // Nature or character of the complaint (e.g., "Sharp", "Dull", "Throbbing")
    @Column(columnDefinition = "TEXT")
    public String natureCharacter;

    // Severity of the complaint (e.g., "Mild", "Moderate", "Severe")
    @Column(columnDefinition = "TEXT")
    public String severity;

    // Onset of the complaint (e.g., "Sudden", "Gradual")
    @Column(columnDefinition = "TEXT")
    public String onset;

    // Course or progression of the complaint (e.g., "Improving", "Worsening", "Stable")
    @Column(columnDefinition = "TEXT")
    public String courseProgression;

    // Aggravating factors (e.g., "Movement", "Cold weather")
    @Column(columnDefinition = "TEXT")
    public String aggravatingFactors;

    // Relieving factors (e.g., "Rest", "Medication")
    @Column(columnDefinition = "TEXT")
    public String relievingFactors;

    // Associated symptoms (e.g., "Nausea", "Fever")
    @Column(columnDefinition = "TEXT")
    public String associatedSymptoms;

    // Many complaints can be associated with one consultation
    @ManyToOne
    @JoinColumn(nullable = false)
    public Consultation consultation;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;
}






