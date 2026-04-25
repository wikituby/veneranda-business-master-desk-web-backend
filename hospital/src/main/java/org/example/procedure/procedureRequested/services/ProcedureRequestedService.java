package org.example.procedure.procedureRequested.services;

import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.GeneralUs;
import org.example.diagnostics.ultrasoundScan.generalUs.services.GeneralUsService;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.invoice.services.InvoiceService;
import org.example.finance.invoice.services.payloads.requests.InvoiceUpdateRequest;
import org.example.lab.generalReport.services.GeneralLabReportService;
import org.example.lab.singleStatementReport.malaria.domains.Malaria;
import org.example.lab.singleStatementReport.malaria.services.MalariaService;
import org.example.lab.urinalysis.services.UrinalysisService;
import org.example.procedure.itemUsedInProcedure.services.ItemUsedService;
import org.example.procedure.procedure.domains.Procedure;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.procedure.procedureRequested.services.payloads.requests.ProcedureRequestedRequest;
import org.example.procedure.procedureRequested.services.payloads.requests.ProcedureRequestedUpdateRequest;
import org.example.procedure.procedureRequested.services.payloads.responses.ProcedureRequestedDTO;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProcedureRequestedService {
    @Inject
    ProcedureRequestedRepository proceduresRequestedRepository;

    @Inject
    InvoiceService invoiceService;

    @Inject
    ItemUsedService itemUsedService;

    @Inject
    GeneralUsService generalUsService;

    @Inject
    MalariaService malariaService;

    @Inject
    GeneralLabReportService generalLabReportService;

    @Inject
    UrinalysisService urinalysisService;
    public static final String NOT_FOUND = "Not found!";
    public static final String VISIT_CLOSED = "Not found!";

    @Transactional
    public Response createNewProcedureRequested(Long visitID, ProcedureRequestedRequest request) {
        // Fetch the PatientVisit and Procedure in one go
        PatientVisit patientVisit = PatientVisit.findById(visitID);

        if (patientVisit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Patient visit not found for ID: " + visitID, null))
                    .build();
        }

        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Please Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }

        // Fetch procedure
        Procedure procedure = Procedure.findById(request.procedureId);
        if (procedure == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Invalid procedure ID: " + request.procedureId, null))
                    .build();
        }

        // Check if it's a consultation category
        if (procedure.category != null && "consultation".equalsIgnoreCase(procedure.category.name)) {
            boolean consultationAlreadyDone = proceduresRequestedRepository
                    .find("category = ?1 and visit.id = ?2", "consultation", visitID)
                    .firstResultOptional()
                    .isPresent();

            if (consultationAlreadyDone) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseMessage("Consultation already done for this visit", null))
                        .build();
            }
        }

        /*User user = User.find(
                "role = ?1",
                "sono"
        ).firstResult();*/



        BigDecimal totalBalanceDue = invoiceService.calculateTotalBalanceDueForClosedVisits(patientVisit.patient.id);

        if (totalBalanceDue.compareTo(BigDecimal.ZERO) > 0 &&
        (patientVisit.patient.patientGroup == null ||
         !(patientVisit.patient.patientGroup.groupName.equalsIgnoreCase("veneranda medical") ||
           patientVisit.patient.patientGroup.groupName.equalsIgnoreCase("katoma child youth development centre - UG1005")))) {
    
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseMessage("Cannot access any service. Patient has a debt of: "
                        + totalBalanceDue + " and doesn't belong to an authorized credit group. Please clear the debt first or contact Admin"))
                .build();
    }
    


        // Throw exception if either of them is not found now


        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }


        // Throw exception if either of them is not found now

       if (procedure.category.parent == null ) {
            //throw new IllegalArgumentException(NOT_FOUND); // Handle not found error
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage(procedure.procedureName + " " + "Service has no category, please give it a category and try again", null))
                    .build();
        }


        // Create a new ProcedureRequested record (same procedure can be requested multiple times per visit)
        ProcedureRequested procedureRequested = new ProcedureRequested();
        procedureRequested.procedure = procedure;
        procedureRequested.patientName = patientVisit.patient.patientFirstName+" "+patientVisit.patient.patientSecondName;
        procedureRequested.quantity = request.quantity;
        procedureRequested.report = "";
        procedureRequested.procedureId = request.procedureId;
        procedureRequested.orderedBy = request.orderedBy;
        //procedureRequested.doneBy = user.username;
        procedureRequested.unitSellingPrice = request.unitSellingPrice;
        procedureRequested.totalAmount = BigDecimal.valueOf(request.quantity).multiply(request.unitSellingPrice);
        procedureRequested.visit = patientVisit;
        //procedureRequested.procedureRequestedType = procedure.procedureType;
        procedureRequested.procedureRequestedName = procedure.procedureName;
        procedureRequested.exam = procedure.procedureName;
        procedureRequested.indication = request.indication;
        procedureRequested.status = "pending";
        procedureRequested.bgColor = "rgb(26, 139, 204)";

        procedureRequested.category = procedure.category != null && procedure.parentCategory != null 
                ? procedure.parentCategory.name : null;
        procedureRequested.dateOfProcedure = java.time.LocalDate.now();
        procedureRequested.timeOfProcedure = java.time.LocalTime.now();

        proceduresRequestedRepository.persist(procedureRequested);

        /*itemUsedService.performProcedure(request.procedureId);

        if (procedure.category != null && "ultrasound scan".equalsIgnoreCase(procedure.category.name)) {
            generalUsService.createGeneralUsReport(procedureRequested);

        }
        if (procedure.category != null && "malaria test".equalsIgnoreCase(procedure.category.name)) {
            malariaService.createMrdtReport(procedureRequested);

        }
        if ("urinalysis".equalsIgnoreCase(procedureRequested.procedureRequestedName)) {
            urinalysisService.createUrinalysisReport(procedureRequested);
        }

        if (procedure.category == null ||
                (!"ultrasound scan".equalsIgnoreCase(procedure.category.name)
                        && !"malaria test".equalsIgnoreCase(procedure.category.name)
                        && !"urinalysis".equalsIgnoreCase(procedureRequested.procedureRequestedName))) {
            generalLabReportService.createGeneralLabReport(procedureRequested);
        }*/

        return Response.ok(new ResponseMessage("New procedure request made successfully", new ProcedureRequestedDTO(procedureRequested))).build();
    }

    @Transactional
    public Response updateMissingProcedureReferences() {
        // Find all ProcedureRequested records where procedure is null but procedureId is not null
        List<ProcedureRequested> procedureRequestedList = ProcedureRequested.find(
                "procedure is null and procedureId is not null"
        ).list();

        int updatedCount = 0;
        int notFoundCount = 0;

        for (ProcedureRequested procedureRequested : procedureRequestedList) {
            if (procedureRequested.procedureId != null) {
                Procedure procedure = Procedure.findById(procedureRequested.procedureId);
                if (procedure != null) {
                    procedureRequested.procedure = procedure;
                    proceduresRequestedRepository.persist(procedureRequested);
                    updatedCount++;
                } else {
                    notFoundCount++;
                }
            }
        }

        String message = String.format(
                "Update completed. Updated: %d records, Procedure not found for: %d records",
                updatedCount,
                notFoundCount
        );

        return Response.ok(new ResponseMessage(message, null)).build();
    }





    public ProcedureRequestedDTO getOtherRequestedProcedureById(Long id) {
        return proceduresRequestedRepository.findByIdOptional(id)
                .map(ProcedureRequestedDTO::new)  // Convert Patient entity to PatientDTO
                .orElseThrow(() -> new WebApplicationException("ProcedureRequested not found", 404));
    }

    public List<ProcedureRequestedDTO> getRequestedProceduresByVisitId(Long visitId) {
        List<ProcedureRequested> requestedProcedures = proceduresRequestedRepository
                .find("visit.id = ?1 ORDER BY id DESC", visitId)
                .list();

        return requestedProcedures.stream()
                .map(ProcedureRequestedDTO::new)
                .collect(Collectors.toList());
    }


    public ProcedureRequestedDTO updateProcedureRequestedById(Long id, ProcedureRequestedUpdateRequest request) {
        Procedure procedure = Procedure.findById(request.procedureId);

        ProcedureRequested procedureReq = ProcedureRequested.findById(id);

        if ("closed".equals(procedureReq.visit.visitStatus)) {
            throw new WebApplicationException("Your new password must be unique",409);

        }
        return proceduresRequestedRepository.findByIdOptional(id)
                .map(procedureRequested -> {

                    procedureRequested.doneBy = request.doneBy;
                    procedureRequested.orderedBy = request.orderedBy;
                    procedureRequested.report = request.report;
                    procedureRequested.indication = request.indication;
                    procedureRequested.quantity = request.quantity;
                    procedureRequested.unitSellingPrice = procedure.unitSellingPrice;
                    procedureRequested.totalAmount = BigDecimal.valueOf(request.quantity).multiply(procedure.unitSellingPrice);
                    //procedureRequested.procedureRequestedType = procedure.procedureType;
                    procedureRequested.procedureRequestedName = procedure.procedureName;
                    procedureRequested.updateDate = LocalDate.now();

                    proceduresRequestedRepository.persist(procedureRequested);

                    return new ProcedureRequestedDTO(procedureRequested);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }









    


    public List<ProcedureRequestedDTO> getLabTestProceduresByVisit(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is "labtest" and visit ID matches, ordered descending
        List<ProcedureRequested> labTestProcedures = ProcedureRequested
                .<ProcedureRequested>find("SELECT DISTINCT p FROM ProcedureRequested p LEFT JOIN FETCH p.procedure LEFT JOIN FETCH p.procedure.category WHERE p.category = ?1 AND p.visit.id = ?2 ORDER BY p.id DESC", "labtest", visitId)
                .list();

        // Convert the results to a list of ProcedureRequestedDTO
        return labTestProcedures.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }


    /*public List<ProcedureRequestedDTO> getAllLabTestProcedures() {
        // Query for ProcedureRequested where procedureRequestedType is "labtest" and visit ID matches, ordered descending
        List<ProcedureRequested> labTests = ProcedureRequested
                .<ProcedureRequested>find("SELECT DISTINCT p FROM ProcedureRequested p LEFT JOIN FETCH p.procedure LEFT JOIN FETCH p.procedure.category WHERE p.category = ?1 ORDER BY p.id DESC", "labtest")
                .list();

        // Convert the results to a list of ProcedureRequestedDTO
        return labTests.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }*/

    public List<ProcedureRequestedDTO> getAllLabTestProcedures() {

        List<ProcedureRequested> labTests = ProcedureRequested
                .<ProcedureRequested>find("""
                    SELECT DISTINCT p
                    FROM ProcedureRequested p
                    JOIN FETCH p.procedure pr
                    JOIN FETCH pr.parentCategory pc
                    WHERE LOWER(pc.name) = LOWER(?1)
                    ORDER BY p.id DESC
                """, "labtest")
                .list();
    
        return labTests.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }



    /*public List<ProcedureRequestedDTO> getAllUltrasoundScanProcedures() {
        // Query for ProcedureRequested where category is "imaging", ordered descending
        List<ProcedureRequested> UltrasoundScan = ProcedureRequested
                .<ProcedureRequested>find("SELECT DISTINCT p FROM ProcedureRequested p LEFT JOIN FETCH p.procedure LEFT JOIN FETCH p.procedure.category WHERE p.category = ?1 ORDER BY p.id DESC", "imaging")
                .list();

        // Convert the results to a list of ProcedureRequestedDTO
        return UltrasoundScan.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }*/

    public List<ProcedureRequestedDTO> getAllUltrasoundScanProcedures() {

        List<ProcedureRequested> ultrasoundScans = ProcedureRequested
                .<ProcedureRequested>find("""
                    SELECT DISTINCT p
                    FROM ProcedureRequested p
                    JOIN FETCH p.procedure pr
                    JOIN FETCH pr.category c
                    WHERE LOWER(c.name) = LOWER(?1)
                    ORDER BY p.id DESC
                """, "ultrasound scan")
                .list();
    
        return ultrasoundScans.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }


    public List<ProcedureRequestedDTO> getAllDentalProcedures() {

        List<ProcedureRequested> dentalProcedures = ProcedureRequested
                .<ProcedureRequested>find(
                        "SELECT DISTINCT p FROM ProcedureRequested p " +
                        "LEFT JOIN FETCH p.procedure pr " +
                        "LEFT JOIN FETCH pr.parentCategory pc " +
                        "WHERE pc.name = ?1 " +
                        "ORDER BY p.id DESC",
                        "dental"
                )
                .list();
    
        return dentalProcedures.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }
    






    public List<ProcedureRequestedDTO> getUltrasoundScanProceduresByVisit(Long visitId) {
        // Query for ProcedureRequested where category is "imaging" and visit ID matches, ordered descending
        List<ProcedureRequested> UltrasoundScan = ProcedureRequested
                .<ProcedureRequested>find("SELECT DISTINCT p FROM ProcedureRequested p LEFT JOIN FETCH p.procedure LEFT JOIN FETCH p.procedure.category WHERE p.category = ?1 AND p.visit.id = ?2 ORDER BY p.id DESC", "imaging", visitId)
                .list();

        // Convert the results to a list of ProcedureRequestedDTO
        return UltrasoundScan.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }


   /*public List<ProcedureRequestedDTO> getNonLabTestNonUltrasoundProceduresByVisit(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is neither "labtest" nor "Ultrasound" and visit ID matches, ordered descending
        List<ProcedureRequested> procedures = ProcedureRequested.find(
                "category NOT IN (?1, ?2, ?3) and visit.id = ?4 ORDER BY id DESC",
                "labtest",
                "imaging",
                "consultation",
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return procedures.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }*/

    public List<ProcedureRequestedDTO> getNonLabTestNonUltrasoundProceduresByVisit(Long visitId) {

        List<ProcedureRequested> procedures = ProcedureRequested.find("""
                SELECT p
                FROM ProcedureRequested p
                JOIN p.procedure pr
                LEFT JOIN pr.parentCategory pc
                LEFT JOIN pr.category c
                WHERE p.visit.id = ?1
                AND (
                        (pc IS NULL OR LOWER(pc.name) <> LOWER(?2))
                    AND (c IS NULL OR LOWER(c.name) NOT IN (?3, ?4))
                )
                ORDER BY p.id DESC
                """,
                visitId,
                "labtest",
                "imaging",
                "consultation"
        ).list();
    
        return procedures.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }


    /*public BigDecimal getLabTestProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> labTestProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "labtest",
                visitId
        ).list();

        BigDecimal totalAmountSum;
        totalAmountSum = labTestProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return totalAmountSum;
    }*/

    public BigDecimal getLabTestProceduresAndSumByVisit(Long visitId) {

        List<ProcedureRequested> labTestProcedures = ProcedureRequested.find("""
                SELECT p
                FROM ProcedureRequested p
                JOIN p.procedure pr
                JOIN pr.parentCategory pc
                WHERE LOWER(pc.name) = LOWER(?1)
                AND p.visit.id = ?2
                ORDER BY p.id DESC
                """,
                "labtest",
                visitId
        ).list();
    
        return labTestProcedures.stream()
                .map(p -> p.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public BigDecimal getScanProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "ultrasound scan",
                visitId
        ).list();

        BigDecimal totalAmountSum;
        totalAmountSum = scanProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return totalAmountSum;
    }

  /*  public BigDecimal getAllScanProcedures() {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find(
                "category = ?1 ORDER BY id DESC",
                "imaging"

        ).list();

        BigDecimal totalAmountSum;
        totalAmountSum = scanProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return totalAmountSum;
    }*/



    public List<BigDecimal> getTotalCostOfProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "Ultrasound",
                visitId
        ).list();

        List<ProcedureRequested> labTestsProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "labtest",
                visitId
        ).list();

        BigDecimal ultrasoundTotalAmount = scanProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal labTotalAmount = labTestsProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return List.of(labTotalAmount, ultrasoundTotalAmount);
    }



    @Transactional
    public Response deleteProcedureRequestById(Long id) {

        ProcedureRequested procedureRequested = proceduresRequestedRepository.findById(id);

        if (procedureRequested == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        GeneralUs generalUs = GeneralUs.find(
                "procedureRequested.id = ?1",
                id

        ).firstResult();

        Malaria malaria = Malaria.find(
                "procedureRequested.id = ?1",
                id

        ).firstResult();

        if ("closed".equals(procedureRequested.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Please Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }

        if (Objects.equals(procedureRequested.category, "imaging")) {
            if (generalUs == null || ((generalUs.findings == null || generalUs.findings.isEmpty()) && (generalUs.impression == null || generalUs.impression.isEmpty()) &&
                    (generalUs.indication == null || generalUs.indication.isEmpty()))) {
                GeneralUs.delete("procedureRequested.id", id);
                Panache.getEntityManager().flush(); // 👈 force flush children deletion

            }else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Cannot delete scan report with findings ", null))
                        .build();
            }

        }

        if (Objects.equals(procedureRequested.procedureRequestedType, "malariatest")) {
            if (malaria == null || ((malaria.bs == null || malaria.bs.trim().isEmpty()) && (malaria.mrdt == null || malaria.mrdt.trim().isEmpty()))
                    ) {
                Malaria.delete("procedureRequested.id", id);
                Panache.getEntityManager().flush(); // 👈 force flush children deletion

            }else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Cannot delete MALARIA report with bs or mrdt results ", null))
                        .build();
            }

        }


        Invoice invoice = Invoice.find("visit.id", procedureRequested.visit.id).firstResult();

        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.tax = BigDecimal.ZERO;
        request.discount = BigDecimal.ZERO;

        invoiceService.updateInvoice(invoice.id, request);

        proceduresRequestedRepository.delete(procedureRequested);

        itemUsedService.restoreStockOnProcedureDelete(procedureRequested.procedureId);

        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }



    // final and working//











}






