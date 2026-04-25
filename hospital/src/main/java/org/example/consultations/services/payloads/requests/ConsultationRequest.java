package org.example.consultations.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

public class ConsultationRequest {
    // A brief medical history of the patient (e.g., "Has a history of hypertension, diabetes")
    @Schema(example = "neck surgery")
    public String medicalHistory;

    // Clinical examination findings (e.g., "Normal physical examination", "Swelling in the left ankle")
    @Schema(example = "oral-pharyngeal pain")
    public String clinicalExamination;

    // List of differential diagnoses considered during the consultation (e.g., "Acute Bronchitis, Pneumonia")
    @Schema(example = "pharyngitis, ")
    public String differentialDiagnosis;

    // The final diagnosis made based on the consultation (e.g., "Pneumonia", "Hypertension")
    @Schema(example = "pharyngitis")
    public String diagnosis;

    @Schema(example = "Doctor")
    public String doneBy;

    @Schema(example = "Done successfully")
    public String report;

    @Schema(example = "Chest Pain")
    public String chiefComplaint;

    @Schema(example = "Chest Pain for 3days")
    public String historyOfPresentingComplaint;

    @Schema(example = "hiv treatment")
    public String medicationHistory;

    @Schema(example = "uticaria")
    public String allergies;

    @Schema(example = "hypertensive history")
    public String familyHistory;

    @Schema(example = "smoking history")
    public String socialHistory;

    @Schema(example = "G3P2, previous CS")
    public String pastObstetricHistory;

    @Schema(example = "Regular cycles, no STI history")
    public String pastGynaecologicalHistory;

    @Schema(example = "normal heart beat")
    public String systemicExamination;

    @Schema(example = "Clear air entry bilaterally")
    public String respiratoryExamination;

    @Schema(example = "Regular rate and rhythm")
    public String cardiovascularExamination;

    @Schema(example = "GCS 15, no focal deficit")
    public String cnsExamination;

    @Schema(example = "Soft, non-tender, no organomegaly")
    public String abdominalExamination;

    @Schema(example = "Full range of movement")
    public String musculoskeletalExamination;

    @Schema(example = "ENT, skin, endocrine unremarkable")
    public String reviewOfOtherSystems;

    @Schema(example = "normal heart beat")
    public String clinicalImpression;

    @Schema(example = "normal heart beat")
    public String followUpInstructions;

    @Schema(example = "normal heart beat")
    public String notes;

    @Schema(description = "List of complaints associated with this consultation")
    public List<ComplaintRequest> complaints;




















}






