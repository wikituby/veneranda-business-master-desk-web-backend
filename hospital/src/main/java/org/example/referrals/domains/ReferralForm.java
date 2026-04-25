package org.example.referrals.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDate;

@Entity
public class ReferralForm extends PanacheEntity {

    // Date of admission to the current facility
    @Column(nullable = false)
    public LocalDate dateOfAdmission;

    // Date when the referral was made
    @Column(nullable = false)
    public LocalDate dateOfReferral;

    // Name of the patient being referred
    @Column(nullable = false)
    public String patientName;

    // Age of the patient
    @Column(nullable = false)
    public Integer patientAge;

    // Gender of the patient
    @Column(nullable = false)
    public String patientGender;

    // Patient's residential address
    @Column(nullable = false)
    public String patientAddress;

    // Reason for referral (e.g., specific medical concern)
    @Column(nullable = false)
    public String reasonForReferral;

    // Facility to which the patient is being referred
    @Column(nullable = false)
    public String facilityReferredTo;

    // Primary diagnosis or impression leading to the referral
    @Column(columnDefinition = "TEXT") // Assuming this may be a longer text
    public String diagnosisOrImpression;

    // Brief medical history relevant to the referral
    @Column(columnDefinition = "TEXT") // Assuming this may be a longer text
    public String briefHistory;

    // Clinical examination findings relevant to the referral
    @Column(columnDefinition = "TEXT")
    public String examinationFindings;

    // Summary of investigations conducted before referral
    @Column(columnDefinition = "TEXT")
    public String investigationsDone;

    // List of treatments administered prior to referral
    @Column(columnDefinition = "TEXT")
    public String treatmentGiven;

    // Name of the referring doctor
    @Column(nullable = false)
    public String referredBy;

    // Phone number of the referring doctor
    @Column(nullable = false)
    public String referringDoctorPhoneNumber;

    // One recommendation is associated with one visit
    @ManyToOne
    @JoinColumn(nullable = false)  // Foreign key to link to the PatientVisit
    public PatientVisit visit;  // Link to the specific visit this recommendation is related to
}






