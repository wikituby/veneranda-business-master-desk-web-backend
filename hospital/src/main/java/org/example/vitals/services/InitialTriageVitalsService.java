package org.example.vitals.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.visit.domains.PatientVisit;
import org.example.vitals.domains.InitialTriageVitals;
import org.example.vitals.domains.repositories.InitialTriageVitalsRepository;
import org.example.vitals.services.payloads.requests.InitialTriageVitalsRequest;
import org.example.vitals.services.payloads.requests.InitialVitalUpdateRequest;
import org.example.vitals.services.payloads.responses.InitialTriageVitalsDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@ApplicationScoped
public class InitialTriageVitalsService {

    @Inject
    InitialTriageVitalsRepository initialTriageVitalsRepository;

    public static final String NOT_FOUND = "Not found!";

    /**
     * Creates a new InitialTriageVitals for the specified patient ID.
     */
    @Transactional
    public Response createNewInitialTriageVitals(Long id, InitialTriageVitalsRequest request) {
        // Fetch the patient by ID from the database
        PatientVisit patientVisit = PatientVisit.findById(id);
        if (patientVisit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Patient visit not found for ID: " + id, null))
                    .build();
        }

        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }

        // Create a new InitialTriageVitals object
        InitialTriageVitals initialTriageVitals = new InitialTriageVitals();

        // Set dateTaken to today if it's null
        initialTriageVitals.dateTaken = (request.dateTaken != null) ? request.dateTaken : LocalDate.now();
        initialTriageVitals.timeTaken = (request.timeTaken != null) ? request.timeTaken : LocalTime.now();
        
        //initialTriageVitals.timeTaken = request.timeTaken;
        initialTriageVitals.spO2 = request.spO2;
        initialTriageVitals.station = request.station;
        initialTriageVitals.height = request.height;
        initialTriageVitals.heartRate = request.heartRate;
        initialTriageVitals.weight = request.weight;
        initialTriageVitals.muac = request.muac;
        initialTriageVitals.temperature = request.temperature;
        initialTriageVitals.systolic = request.systolic;
        initialTriageVitals.diastolic = request.diastolic;
        initialTriageVitals.pulseRate = request.pulseRate;
        initialTriageVitals.takenBy = request.takenBy;
        initialTriageVitals.respiratoryRate = request.respiratoryRate;



        // Ensure systolic and diastolic values are not null before calculating MAP
        if (request.systolic == null || request.diastolic == null) {
            initialTriageVitals.map = null;
            initialTriageVitals.bloodPressure = "N/A";
        }

        initialTriageVitals.bloodPressure = request.systolic +"/"+ request.diastolic;

        initialTriageVitals.visit = patientVisit;

        initialTriageVitals.bmi = InitialTriageVitals.computeBmi(initialTriageVitals.weight, initialTriageVitals.height);

        // Persist the new initial triage vitals
        initialTriageVitalsRepository.persist(initialTriageVitals);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patient vitals saved successfully", new InitialTriageVitalsDTO(initialTriageVitals)))
                .build();
    }



    @Transactional
    public List<InitialTriageVitalsDTO> getAllInitialTriageVitals() {
        return initialTriageVitalsRepository.listAll(Sort.descending("id"))
                .stream()
                .map(InitialTriageVitalsDTO::new)
                .toList();
    }


   /* public InitialTriageVitalsDTO getInitialTriageVitalById(Long id) {
        return initialTriageVitalsRepository.findByIdOptional(id)
                .map(InitialTriageVitalsDTO::new)  // Convert InitialTriageVital entity to InitialTriageVitalsDTO
                .orElseThrow(() -> new WebApplicationException("Patient not found", 404));
    }*/



    public List<InitialTriageVitalsDTO> getInitialTriageVitalsByVisitId(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is "labtest" and visit ID matches, ordered descending
        List<InitialTriageVitals> initialTriageVitals = InitialTriageVitals.find(
                "visit.id = ?1 ORDER BY id DESC", // Replace 'id' with your desired field for sorting
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return initialTriageVitals.stream()
                .map(InitialTriageVitalsDTO::new)
                .toList();
    }

    public Response updateInitialVitalById(Long id, InitialVitalUpdateRequest request) {

        InitialTriageVitals initialTriageVitals1 = initialTriageVitalsRepository.findById(id);

        if ("closed".equals(initialTriageVitals1.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }


        return initialTriageVitalsRepository.findByIdOptional(id)
                .map(initialTriageVitals -> {

                    initialTriageVitals.dateTaken = (request.dateTaken != null) ? request.dateTaken : LocalDate.now();
                    initialTriageVitals.timeTaken = (request.timeTaken != null) ? request.timeTaken : LocalTime.now();

                    initialTriageVitals.spO2 = request.spO2;
                    initialTriageVitals.station = request.station;
                    initialTriageVitals.height = request.height;
                    initialTriageVitals.heartRate = request.heartRate;
                    initialTriageVitals.weight = request.weight;
                    initialTriageVitals.muac = request.muac;
                    initialTriageVitals.temperature = request.temperature;
                    initialTriageVitals.systolic = request.systolic;
                    initialTriageVitals.diastolic = request.diastolic;
                    initialTriageVitals.pulseRate = request.pulseRate;
                    initialTriageVitals.takenBy = request.takenBy;

                    // Calculate Mean Arterial Pressure (MAP)
                    initialTriageVitals.map = request.diastolic + (request.systolic - request.diastolic) / 3.0;

                    initialTriageVitals.respiratoryRate = request.respiratoryRate;
                    initialTriageVitals.bloodPressure = request.systolic + "/" + request.diastolic;

                    initialTriageVitals.bmi = InitialTriageVitals.computeBmi(initialTriageVitals.weight, initialTriageVitals.height);

                    initialTriageVitalsRepository.persist(initialTriageVitals);

                    //return new InitialTriageVitalsDTO(initialTriageVitals);
                    return Response.ok(new ResponseMessage("Vital Updated successfully", new InitialTriageVitalsDTO(initialTriageVitals))).build();

                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    @Transactional
    public Response deleteVitalById(Long id) {

        InitialTriageVitals initialTriageVitals = initialTriageVitalsRepository.findById(id);

        if (initialTriageVitals == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        if ("closed".equals(initialTriageVitals.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }

        initialTriageVitalsRepository.delete(initialTriageVitals);

        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }





























}






