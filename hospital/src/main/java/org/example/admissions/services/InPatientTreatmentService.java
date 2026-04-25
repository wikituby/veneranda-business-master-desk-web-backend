package org.example.admissions.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.admissions.domains.InPatientTreatment;
import org.example.admissions.domains.repositories.InPatientTreatmentRepository;
import org.example.admissions.services.payloads.responses.InPatientTreatmentDTO;
import org.example.admissions.services.payloads.requests.InPatientTreatmentRequest;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDate;
import java.time.LocalTime;

@ApplicationScoped
public class InPatientTreatmentService {

    @Inject
    InPatientTreatmentRepository inPatientTreatmentRepository;

    private static final String NOT_FOUND = "Not found!";

    public InPatientTreatmentDTO createNewInPatientTreatment(InPatientTreatmentRequest request){
        InPatientTreatment inPatientTreatment = new InPatientTreatment();
        PatientVisit patientVisit = PatientVisit.findById(request.visitId);

        inPatientTreatment.medicine = request.medicine;
        inPatientTreatment.route = request.route;
        inPatientTreatment.dose = request.dose;
        inPatientTreatment.frequency = request.frequency;
        inPatientTreatment.dateGiven = LocalDate.now();
        inPatientTreatment.timeGiven = LocalTime.now();
        inPatientTreatment.administeredByInitials = request.administeredByInitials;
        inPatientTreatment.visit = patientVisit;

        inPatientTreatmentRepository.persist(inPatientTreatment);

        return new InPatientTreatmentDTO(inPatientTreatment);

    }

}






