package org.example.cafeteria.sales.saleDay.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.cafeteria.client.domains.Buyer;
import org.example.cafeteria.client.domains.repositories.BuyerRepository;
import org.example.cafeteria.finance.invoice.services.CanteenInvoiceService;
import org.example.cafeteria.sales.saleDay.domains.SaleDay;
import org.example.cafeteria.sales.saleDay.domains.repository.SaleDayRepository;
import org.example.cafeteria.sales.saleDay.services.payloads.requests.SaleDayRequest;
import org.example.cafeteria.sales.saleDay.services.payloads.requests.SaleDayStatusUpdateRequest;
import org.example.cafeteria.sales.saleDay.services.payloads.requests.SaleDayUpdateRequest;
import org.example.cafeteria.sales.saleDay.services.payloads.responses.SaleDayDTO;
import org.example.configuration.handler.ResponseMessage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class SaleDayService {

    @Inject
    SaleDayRepository saleDayRepository;

    @Inject
    BuyerRepository patientRepository;

    @Inject
    CanteenInvoiceService invoiceService;

    public static final String NOT_FOUND = "Not found!";

    /**
     * Creates a new PatientVisit for the specified patient ID.
     */
    @Transactional
    public SaleDayDTO createNewPatientVisit(Long id, SaleDayRequest request) {
        // Fetch the patient
        Buyer patient = Buyer.findById(id);
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found for ID: " + id);
        }


        List<SaleDay> openVisits = SaleDay.find("patient.id = ?1 and visitStatus = ?2", id, "open").list();

        if (!openVisits.isEmpty()) {
            throw new IllegalStateException("Please first Close the open visits before opening a new one.");
        }


        //Generate unique visit number
        int lastVisitNumber = generateVisitNo(id);
        int newVisitNumber = lastVisitNumber + 1;

        // Ensure visitNumber is unique
        boolean exists = saleDayRepository.find("patient.id = ?1 and visitNumber = ?2", id, newVisitNumber)
                .firstResultOptional()
                .isPresent();
        if (exists) {
            throw new IllegalArgumentException("Duplicate visitNumber: " + newVisitNumber + " for patient ID " + id);
        }

        // Create a new visit
        SaleDay patientVisit = new SaleDay();
        patientVisit.visitDate = LocalDate.now();
        patientVisit.visitTime = LocalTime.now();
        patientVisit.visitReason = "Consultation";
        patientVisit.visitStatus = "open";
        patientVisit.visitType = request.visitType;
        patientVisit.visitNumber = newVisitNumber;
        patientVisit.visitName = "Visit 0" + newVisitNumber;
        patientVisit.patient = patient;


        // Persist visit
        saleDayRepository.persist(patientVisit);

        invoiceService.createInvoice(patientVisit.id);

        return new SaleDayDTO(patientVisit);

    }
    

    /**
     * Generates the next visit number for a given patient by fetching the highest existing visit number.
     */
    @Transactional
    public int generateVisitNo(Long patientId) {
        return saleDayRepository.find("patient.id = ?1", Sort.descending("visitNumber"), patientId)
                .list()  // Sort by visit number in descending order
                .stream()
                .map(patientVisit -> patientVisit.visitNumber)  // Map to visit number
                .findFirst()  // Get the highest visit number, if exists
                .orElse(0);  // Return 0 if no visit found
    }
    @Transactional
    public int generateVisitNos(Long patientId) {
        // Query the highest visit number for the given patient
        Integer highestVisitNumber = saleDayRepository.find("patient.id = ?1", Sort.descending("visitNumber"), patientId)
                .project(Integer.class) // Fetch the `visitNumber` as Integer
                .firstResult(); // Get the first result (highest visit number)

        // If no visits exist, default to 0; otherwise, add 1
        return (highestVisitNumber != null ? highestVisitNumber : 0) + 1;
    }

    @Transactional
    public List<SaleDayDTO> getAllPatientVisits() {
        // Retrieve all PatientVisit records from the repository
        List<SaleDay> patientVisits = saleDayRepository.listAll();

        // Map each PatientVisit to a PatientVisitDTO
        return patientVisits.stream()
                .map(SaleDayDTO::new)  // Mapping each entity to its DTO
                .collect(Collectors.toList());
    }

    @Transactional
    public List<SaleDayDTO> getAllPatients() {
        return saleDayRepository.listAll(Sort.ascending("visitNumber"))
                .stream()
                .map(SaleDayDTO::new)
                .toList();
    }

    public List<SaleDayDTO> getVisitByPatientId(Long patientId) {
        // Fetch the list of initial triage vitals by visitId
        List<SaleDayDTO> result = saleDayRepository
                .find("patient.id", patientId)
                .list()  // Fetch the result as a List
                .stream()  // Convert the result into a stream for further transformation
                .map(SaleDayDTO::new)  // Map each entity to a DTO
                .toList();  // Collect the mapped entities into a list

        if (result.isEmpty()) {
            // If no results found, log the error and throw a 404 exception
            String errorMessage = String.format("No patient visits found for patientId: %d", patientId);
            LOG.error(errorMessage);
            throw new WebApplicationException(errorMessage, Response.Status.NOT_FOUND);
        }

        // Return the list of DTOs
        return result;
    }

    @Transactional
    public Response getLatestVisitByPatientId(Long patientId) {
        SaleDay latestVisit = saleDayRepository
                .find("patient.id", Sort.descending("id"), patientId)
                .firstResult();

        SaleDayDTO latestVisitDTO;

        if (latestVisit == null) {
            SaleDayRequest defaultRequest = new SaleDayRequest();
            defaultRequest.visitType = "General Consultation";


            // Create a new visit
            latestVisitDTO = createNewPatientVisit(patientId, defaultRequest);
        } else {
            // Convert the found PatientVisit into DTO
            latestVisitDTO = new SaleDayDTO(latestVisit);
        }

        return Response.ok(new ResponseMessage("Patient visit fetched successfully", latestVisitDTO)).build();
    }





    public SaleDayDTO updatePatientVisitById(Long id, SaleDayUpdateRequest request) {
        return saleDayRepository.findByIdOptional(id)
                .map(patientVisit -> {

                    patientVisit.visitType = request.visitType;
                    patientVisit.visitReason = request.visitReason;
                    patientVisit.visitLastUpdatedDate = LocalDate.now();

                    saleDayRepository.persist(patientVisit);

                    return new SaleDayDTO(patientVisit);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public SaleDayDTO updatePatientVisitStatusById(Long id, SaleDayStatusUpdateRequest request) {
        return saleDayRepository.findByIdOptional(id)
                .map(patientVisit -> {

                    patientVisit.visitStatus = request.visitStatus;
                    patientVisit.visitLastUpdatedDate = LocalDate.now();

                    saleDayRepository.persist(patientVisit);

                    return new SaleDayDTO(patientVisit);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }






}






