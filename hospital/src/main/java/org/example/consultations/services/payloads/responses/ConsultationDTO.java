package org.example.consultations.services.payloads.responses;

import org.example.consultations.domains.Consultation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ConsultationDTO {
    public Long id;  // The unique identifier of the consultation
    public String medicalHistory;  // A brief medical history of the patient
    public String clinicalExamination;  // Clinical examination findings
    public String differentialDiagnosis;  // List of differential diagnoses
    public String diagnosis;  // Final diagnosis based on the consultation
    public String historyOfPresentingComplaint;  // History of presenting complaint
    public String chiefComplaint;  // Chief complaint
    public String medicationHistory;  // Medication history
    public String allergies;  // Allergies
    public String familyHistory;  // Family history
    public String socialHistory;  // Social history
    public String pastObstetricHistory;
    public String pastGynaecologicalHistory;
    public String systemicExamination;  // Systemic examination
    public String respiratoryExamination;
    public String cardiovascularExamination;
    public String cnsExamination;
    public String abdominalExamination;
    public String musculoskeletalExamination;
    public String reviewOfOtherSystems;
    public String clinicalImpression;  // Clinical impression
    public String followUpInstructions;  // Follow-up instructions
    public String notes;  // Notes
    public String doneBy;  // Done by
    public String report;  // Report
    public BigDecimal consultationFee;
    public Long visitId;  // The ID of the associated patient visit
    public LocalDate creationDate;  // Creation date
    public LocalDate updateDate;  // Update date
    public List<ComplaintDTO> complaints;  // List of complaints associated with this consultation
    public List<PresentingComplaintDTO> presentingComplaints;  // Structured presenting complaints
    public List<PhysicalExaminationDTO> physicalExaminations;  // Structured physical examinations
    public List<DiagnosisDTO> diagnoses;   // Structured diagnoses (each may include linked treatments)

    public ConsultationDTO(Consultation consultation) {
        this.id = consultation.id;
        this.consultationFee = consultation.consultationFee;
        this.historyOfPresentingComplaint = consultation.historyOfPresentingComplaint;
        this.chiefComplaint = consultation.chiefComplaint;
        this.medicalHistory = consultation.medicalHistory;
        this.clinicalExamination = consultation.clinicalExamination;
        this.differentialDiagnosis = consultation.differentialDiagnosis;
        this.diagnosis = consultation.diagnosis;
        this.medicationHistory = consultation.medicationHistory;
        this.allergies = consultation.allergies;
        this.familyHistory = consultation.familyHistory;
        this.socialHistory = consultation.socialHistory;
        this.pastObstetricHistory = consultation.pastObstetricHistory;
        this.pastGynaecologicalHistory = consultation.pastGynaecologicalHistory;
        this.systemicExamination = consultation.systemicExamination;
        this.respiratoryExamination = consultation.respiratoryExamination;
        this.cardiovascularExamination = consultation.cardiovascularExamination;
        this.cnsExamination = consultation.cnsExamination;
        this.abdominalExamination = consultation.abdominalExamination;
        this.musculoskeletalExamination = consultation.musculoskeletalExamination;
        this.reviewOfOtherSystems = consultation.reviewOfOtherSystems;
        this.clinicalImpression = consultation.clinicalImpression;
        this.followUpInstructions = consultation.followUpInstructions;
        this.notes = consultation.notes;
        this.doneBy = consultation.doneBy;
        this.report = consultation.report;
        this.visitId = consultation.visit != null ? consultation.visit.id : null;
        this.creationDate = consultation.creationDate;
        this.updateDate = consultation.updateDate;
        this.complaints = consultation.complaints != null 
            ? consultation.complaints.stream().map(ComplaintDTO::new).collect(Collectors.toList())
            : List.of();
        this.presentingComplaints = consultation.presentingComplaints != null
            ? consultation.presentingComplaints.stream().map(PresentingComplaintDTO::new).collect(Collectors.toList())
            : List.of();
        this.physicalExaminations = consultation.physicalExaminations != null
            ? consultation.physicalExaminations.stream().map(PhysicalExaminationDTO::new).collect(Collectors.toList())
            : List.of();
        this.diagnoses = consultation.diagnoses != null
            ? consultation.diagnoses.stream().map(DiagnosisDTO::new).collect(Collectors.toList())
            : List.of();
    }
}






