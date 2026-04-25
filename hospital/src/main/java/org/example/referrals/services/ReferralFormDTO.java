package org.example.referrals.services;

import org.example.referrals.domains.ReferralForm;

import java.time.LocalDate;

public class ReferralFormDTO {
    public Long id;  // The unique identifier of the ReferralForm
    public LocalDate dateOfAdmission;  // Date of admission to the current facility
    public LocalDate dateOfReferral;  // Date when the referral was made
    public String patientName;  // Name of the patient being referred
    public Integer patientAge;  // Age of the patient
    public String patientGender;  // Gender of the patient
    public String patientAddress;  // Patient's residential address
    public String reasonForReferral;  // Reason for referral (e.g., specific medical concern)
    public String facilityReferredTo;  // Facility to which the patient is being referred
    public String diagnosisOrImpression;  // Primary diagnosis or impression leading to the referral
    public String briefHistory;  // Brief medical history relevant to the referral
    public String examinationFindings;  // Clinical examination findings relevant to the referral
    public String investigationsDone;  // Summary of investigations conducted before referral
    public String treatmentGiven;  // List of treatments administered prior to referral
    public String referredBy;  // Name of the referring doctor
    public String referringDoctorPhoneNumber;  // Phone number of the referring doctor
    public Long visitId;  // The ID of the associated PatientVisit

    public ReferralFormDTO(ReferralForm referralForm) {
        this.id = referralForm.id;
        this.dateOfAdmission = referralForm.dateOfAdmission;
        this.dateOfReferral = referralForm.dateOfReferral;
        this.patientName = referralForm.patientName;
        this.patientAge = referralForm.patientAge;
        this.patientGender = referralForm.patientGender;
        this.patientAddress = referralForm.patientAddress;
        this.reasonForReferral = referralForm.reasonForReferral;
        this.facilityReferredTo = referralForm.facilityReferredTo;
        this.diagnosisOrImpression = referralForm.diagnosisOrImpression;
        this.briefHistory = referralForm.briefHistory;
        this.examinationFindings = referralForm.examinationFindings;
        this.investigationsDone = referralForm.investigationsDone;
        this.treatmentGiven = referralForm.treatmentGiven;
        this.referredBy = referralForm.referredBy;
        this.referringDoctorPhoneNumber = referralForm.referringDoctorPhoneNumber;
        this.visitId = referralForm.visit != null ? referralForm.visit.id : null;
    }
}






