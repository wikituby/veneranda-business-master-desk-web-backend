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
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Consultation extends PanacheEntity {

    // A brief medical history of the patient (e.g., "Has a history of hypertension, diabetes")
    @Column(columnDefinition = "TEXT")
    public String medicalHistory;

    @Column
    public BigDecimal consultationFee;

    // Clinical examination findings (e.g., "Normal physical examination", "Swelling in the left ankle")
    @Column(columnDefinition = "TEXT")
    public String clinicalExamination;

    // List of differential diagnoses considered during the consultation (e.g., "Acute Bronchitis, Pneumonia")
    @Column(columnDefinition = "TEXT")
    public String differentialDiagnosis;

    // The final diagnosis made based on the consultation (e.g., "Pneumonia", "Hypertension")
    @Column(columnDefinition = "TEXT")
    public String diagnosis;

    @Column(columnDefinition = "TEXT")
    public String doneBy;

    @Column(columnDefinition = "TEXT")
    public String report;

    @Column(columnDefinition = "TEXT")
    public String chiefComplaint;

    @Column(columnDefinition = "TEXT")
    public String historyOfPresentingComplaint;

    @Column(columnDefinition = "TEXT")
    public String medicationHistory;

    @Column(columnDefinition = "TEXT")
    public String allergies;

    @Column(columnDefinition = "TEXT")
    public String familyHistory;

    @Column(columnDefinition = "TEXT")
    public String socialHistory;

    @Column(columnDefinition = "TEXT")
    public String pastObstetricHistory;

    @Column(columnDefinition = "TEXT")
    public String pastGynaecologicalHistory;

    @Column(columnDefinition = "TEXT")
    public String systemicExamination;

    @Column(columnDefinition = "TEXT")
    public String respiratoryExamination;

    @Column(columnDefinition = "TEXT")
    public String cardiovascularExamination;

    @Column(columnDefinition = "TEXT")
    public String cnsExamination;

    @Column(columnDefinition = "TEXT")
    public String abdominalExamination;

    @Column(columnDefinition = "TEXT")
    public String musculoskeletalExamination;

    @Column(columnDefinition = "TEXT")
    public String reviewOfOtherSystems;

    @Column(columnDefinition = "TEXT")
    public String clinicalImpression;

    @Column(columnDefinition = "TEXT")
    public String followUpInstructions;

    @Column(columnDefinition = "TEXT")
    public String notes;

    // One recommendation is associated with one visit
    @ManyToOne
    @JoinColumn(nullable = false)  // Foreign key to link to the PatientVisit
    public PatientVisit visit;  // Link to the specific visit this recommendation is related to

    // One consultation can have many complaints
    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<Complaint> complaints = new ArrayList<>();

    // Structured presenting complaints (SOCRATES-style rows)
    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<PresentingComplaint> presentingComplaints = new ArrayList<>();

    // Structured physical / clinical examinations
    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<PhysicalExamination> physicalExaminations = new ArrayList<>();

    // Structured diagnoses for this consultation (each may link treatments)
    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<Diagnosis> diagnoses = new ArrayList<>();

    // You can add other fields as needed to capture consultation details

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;
}






