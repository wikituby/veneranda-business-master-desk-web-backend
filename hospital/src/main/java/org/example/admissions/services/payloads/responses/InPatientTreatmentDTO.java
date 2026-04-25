package org.example.admissions.services.payloads.responses;

import org.example.admissions.domains.InPatientTreatment;

import java.time.LocalDate;
import java.time.LocalTime;

public class InPatientTreatmentDTO {
    public Long id;  // The unique identifier of the InPatientTreatment record
    public LocalDate dateGiven;  // The date when the treatment was given
    public LocalTime timeGiven;  // The time when the treatment was administered
    public String medicine;  // The name of the medicine given to the patient
    public String dose;  // The dose of the medicine
    public String frequency;  // The frequency of administering the medicine (e.g., once a day)
    public String route;  // The route of administration (e.g., oral, intravenous)
    public String administeredByInitials;  // The initials of the healthcare provider who administered the treatment
    public Long visitId;  // The ID of the associated PatientVisit

    public InPatientTreatmentDTO(InPatientTreatment inPatientTreatment) {
        this.id = inPatientTreatment.id;
        this.dateGiven = inPatientTreatment.dateGiven;
        this.timeGiven = inPatientTreatment.timeGiven;
        this.medicine = inPatientTreatment.medicine;
        this.dose = inPatientTreatment.dose;
        this.frequency = inPatientTreatment.frequency;
        this.route = inPatientTreatment.route;
        this.administeredByInitials = inPatientTreatment.administeredByInitials;
        this.visitId = inPatientTreatment.visit != null ? inPatientTreatment.visit.id : null;
    }
}






