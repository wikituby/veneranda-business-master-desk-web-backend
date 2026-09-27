package org.example.visit.services;

import java.io.IOException;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import io.quarkus.panache.common.Sort;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.PatientGroup;
import org.example.client.domains.repositories.PatientGroupRepository;
import org.example.client.services.PatientGroupService;
import org.example.client.services.PatientService;
import org.example.client.services.payloads.responses.dtos.PatientDTO;
import org.example.client.services.payloads.responses.dtos.PatientGroupDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.client.domains.Patient;
import org.example.client.domains.repositories.PatientRepository;
import org.example.consultations.domains.ConsultationRepository;
import org.example.consultations.services.ConsultationService;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.invoice.domains.repositories.InvoiceRepository;
import org.example.finance.invoice.services.FooterHelperInvoice;
import org.example.finance.invoice.services.InvoiceService;
import org.example.subscription.services.FacilityBranding;
import org.example.subscription.services.FacilityBrandingService;
import org.example.subscription.services.FacilityPdfLogoService;
import org.example.subscription.services.SpecialPrivilegeService;
import org.example.configuration.security.AuthenticatedUserResolver;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.visit.domains.PatientVisit;
import org.example.visit.domains.repositories.PatientVisitRepository;
import org.example.visit.services.paloads.requests.PatientVisitRequest;
import org.example.visit.services.paloads.requests.PatientVisitStatusUpdateRequest;
import org.example.visit.services.paloads.requests.PatientVisitUpdateRequest;
import org.example.visit.services.paloads.requests.VisitParametersRequest;
import org.example.visit.services.paloads.responses.FullVisitResponse;
import org.example.visit.services.paloads.responses.PatientVisitDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

import com.itextpdf.layout.element.Image;

import java.io.InputStream;








import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class PatientVisitService {

    @Inject
    PatientVisitRepository patientVisitRepository;

    @Inject
    ProcedureRequestedRepository procedureRequestedRepository;

    @Inject
    PatientRepository patientRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    InvoiceService invoiceService;

    @Inject
    ConsultationService consultationService;

    @Inject
    PatientGroupService patientGroupService;

    @Inject
    PatientGroupRepository patientGroupRepository;

    @Inject
    ConsultationRepository consultationRepository;

    @Inject
    FacilityBrandingService facilityBrandingService;

    @Inject
    FacilityPdfLogoService facilityPdfLogoService;

    @Inject
    CompassionGroupInvoiceDocxService compassionGroupInvoiceDocxService;

    @Inject
    SpecialPrivilegeService specialPrivilegeService;

    @Inject
    AuthenticatedUserResolver authenticatedUserResolver;

    private FacilityBranding facilityBranding() {
        return facilityBrandingService.resolveDefaultBranding();
    }

    public static final String NOT_FOUND = "Not found!";

    public Long patientRequestedId;

    /**
     * Creates a new PatientVisit for the specified patient ID.
     */
    @Transactional
    public PatientVisitDTO createNewPatientVisit(Long id, PatientVisitRequest request) {
        // Fetch the patient
        Patient patient = Patient.findById(id);
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found for ID: " + id);
        }

        boolean allowConcurrentOpen = request != null && Boolean.TRUE.equals(request.allowConcurrentOpen);
        List<PatientVisit> openVisits = PatientVisit.find("patient.id = ?1 and visitStatus = ?2", id, "open").list();
        if (!openVisits.isEmpty() && !allowConcurrentOpen) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ResponseMessage(
                                    "Please first close the open visit before opening a new one.", null))
                            .build());
        }

        // Generate unique visit number
        int lastVisitNumber = generateVisitNo(id);
        int newVisitNumber = lastVisitNumber + 1;

        // Ensure visitNumber is unique
        boolean exists = patientVisitRepository.find("patient.id = ?1 and visitNumber = ?2", id, newVisitNumber)
                .firstResultOptional()
                .isPresent();
        if (exists) {
            throw new IllegalArgumentException("Duplicate visitNumber: " + newVisitNumber + " for patient ID " + id);
        }

        // Create a new visit
        PatientVisit patientVisit = new PatientVisit();
        patientVisit.visitDate = request.visitDate != null ? request.visitDate : LocalDate.now();
        patientVisit.visitTime = request.visitTime != null ? request.visitTime : LocalTime.now();
        patientVisit.visitReason = hasText(request.visitReason) ? request.visitReason : "Consultation";

        copyPatientSnapshotToVisit(patientVisit, patient);
        applyPatientVisitCreateRequest(patientVisit, request);

        // PROPER NULL CHECKING - fix the error
        if (patient.patientGroup != null && patient.patientGroup.id != null) {
            PatientGroupDTO group = patientGroupService.getPatientGroupById(patient.patientGroup.id);
            patientVisit.visitGroup = group != null ? group.groupNameShortForm : null;
        } else {
            patientVisit.visitGroup = null;
        }

        patientVisit.visitStatus = "open";
        patientVisit.visitType = request.visitType;
        patientVisit.visitNumber = newVisitNumber;
        patientVisit.visitName = "Visit 0" + newVisitNumber;
        patientVisit.patient = patient;
        patientVisit.balanceDue = BigDecimal.valueOf(0.00);
        patientVisit.amountPaid = BigDecimal.valueOf(0.00);
        patientVisit.totalAmount = BigDecimal.valueOf(0.00);
        patientVisit.totalSell = BigDecimal.valueOf(0.00);
        patientVisit.subTotal = BigDecimal.valueOf(0.00);
        patientVisit.tax = BigDecimal.valueOf(0.00);
        patientVisit.discount = BigDecimal.valueOf(0.00);
        patientVisit.totalCost = BigDecimal.valueOf(0.00);
        patientVisit.treatmentTotalSell = BigDecimal.valueOf(0.00);
        patientVisit.totalSellForProcedure = BigDecimal.valueOf(0.00);
        patientVisit.totalCostOfProcedures = BigDecimal.valueOf(0.00);
        patientVisit.totalCostOfTreatment = BigDecimal.valueOf(0.00);

        // Persist visit
        patientVisitRepository.persist(patientVisit);

        invoiceService.createInvoice(patientVisit.id);
        consultationService.newConsultationOnTheGo(patientVisit.id);

        return new PatientVisitDTO(patientVisit);
    }





    @Transactional
    public void updateAllVisitGroupsAndFinancialsFromPatients() {
        // Fetch all patient visits
        List<PatientVisit> patientVisits = patientVisitRepository.listAll();

        // Update each visit with patient group name and financial data
        patientVisits.forEach(visit -> {
            // Update patient group and name
            if (visit.patient != null) {
                // Update patient group if available
                if (visit.patient.getPatientGroup() != null &&
                        visit.patient.getPatientGroup().getId() != null) {

                    PatientGroupDTO group = patientGroupService.getPatientGroupById(visit.patient.getPatientGroup().getId());
                    visit.visitGroup = group != null ? group.groupNameShortForm : null;
                } else {
                    visit.visitGroup = null;
                }

                Patient patient = Patient.findById(visit.patient.id);
                if (patient != null) {
                    copyPatientSnapshotToVisit(visit, patient);
                } else {
                    visit.patientName = "Unknown Patient";
                }
            } else {
                visit.visitGroup = null;
                visit.patientName = "Unknown Patient";
            }

            // Update financial fields from invoice
            updateFinancialFieldsFromInvoice(visit);
        });

        // Persist all updates
        patientVisitRepository.persist(patientVisits);



    }

    @Transactional
    public void fixProcedureRequestedNames() {
        // Fetch all ProcedureRequested where procedureRequestedName is null or empty
        List<ProcedureRequested> proceduresToFix = ProcedureRequested.list(
                "procedureRequestedName IS NULL OR procedureRequestedName = ''"
        );

        proceduresToFix.forEach(proc -> {
            if (proc.procedureRequestedType != null) {
                proc.procedureRequestedName = proc.procedureRequestedType;
            } else {
                proc.procedureRequestedName = "Unknown Procedure"; // fallback if type is null
            }
        });

        // Persist all updates
        procedureRequestedRepository.persist(proceduresToFix);
    }



    private void updateFinancialFieldsFromInvoice(PatientVisit visit) {
        if (visit == null || visit.id == null) {
            return;
        }
        // Recalculate from treatments, procedures, payments — not just copy stored invoice fields.
        invoiceService.syncInvoiceTotalsForVisit(visit.id);
    }

    

    /**
     * Generates the next visit number for a given patient by fetching the highest existing visit number.
     */
    @Transactional
    public int generateVisitNo(Long patientId) {
        return patientVisitRepository.find("patient.id = ?1", Sort.descending("visitNumber"), patientId)
                .list()  // Sort by visit number in descending order
                .stream()
                .map(patientVisit -> patientVisit.visitNumber)  // Map to visit number
                .findFirst()  // Get the highest visit number, if exists
                .orElse(0);  // Return 0 if no visit found
    }
    @Transactional
    public int generateVisitNos(Long patientId) {
        // Query the highest visit number for the given patient
        Integer highestVisitNumber = patientVisitRepository.find("patient.id = ?1", Sort.descending("visitNumber"), patientId)
                .project(Integer.class) // Fetch the `visitNumber` as Integer
                .firstResult(); // Get the first result (highest visit number)

        // If no visits exist, default to 0; otherwise, add 1
        return (highestVisitNumber != null ? highestVisitNumber : 0) + 1;
    }

    @Transactional
    public List<PatientVisitDTO> getAllPatientVisits() {
        // Retrieve all PatientVisit records from the repository
        List<PatientVisit> patientVisits = patientVisitRepository.listAll();

        // Map each PatientVisit to a PatientVisitDTO
        return patientVisits.stream()
                .map(PatientVisitDTO::new)  // Mapping each entity to its DTO
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PatientVisitDTO> getAllPatients() {
        return patientVisitRepository.listAll(Sort.ascending("visitNumber"))
                .stream()
                .map(PatientVisitDTO::new)
                .toList();
    }

    @Transactional
    public List<PatientVisitDTO> getVisitByPatientId(Long patientId) {
        // Fetch the list of initial triage vitals by visitId
        List<PatientVisitDTO> result = patientVisitRepository
                .find("patient.id", patientId)
                .list()  // Fetch the result as a List
                .stream()  // Convert the result into a stream for further transformation
                .map(PatientVisitDTO::new)  // Map each entity to a DTO
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

    /**
     * Lightweight order/visit rows for pickers (POS Select order, etc.).
     * Skips nested collections and financial recomputes that make the full list slow.
     */
    @Transactional
    public List<PatientVisitDTO> getVisitSummariesByPatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return List.of();
        }
        List<PatientVisit> visits = patientVisitRepository
                .find("patient.id", Sort.by("visitNumber").descending().and("id").descending(), patientId)
                .list();
        List<PatientVisitDTO> rows = new ArrayList<>(visits.size());
        for (PatientVisit visit : visits) {
            rows.add(PatientVisitDTO.forListRow(visit, null));
        }
        return rows;
    }

    @Transactional
    public PatientVisitDTO getPatientVisitByVisitId(Long visitId) {
        return patientVisitRepository.findByIdOptional(visitId)
                .map(PatientVisitDTO::new)
                .orElseThrow(() -> new WebApplicationException(NOT_FOUND, 404));
    }

    @Transactional
    public Response getLatestVisitByPatientId(Long patientId) {
        PatientVisit latestVisit = patientVisitRepository
                .find("patient.id", Sort.descending("id"), patientId)
                .firstResult();

        if (latestVisit == null) {
            return Response.ok(new ResponseMessage("No visit found for this patient", null)).build();
        }

        PatientVisitDTO latestVisitDTO = new PatientVisitDTO(latestVisit);
        return Response.ok(new ResponseMessage("Patient visit fetched successfully", latestVisitDTO)).build();
    }




    @Transactional
    public PatientVisitDTO updatePatientVisitById(Long id, PatientVisitUpdateRequest request) {
        return patientVisitRepository.findByIdOptional(id)
                .map(patientVisit -> {
                    applyPatientVisitUpdateRequest(patientVisit, request);
                    patientVisit.visitLastUpdatedDate = LocalDate.now();
                    patientVisitRepository.persist(patientVisit);

                    if (request != null
                            && patientVisit.patient != null
                            && isHighestVisitNumberForPatient(patientVisit)) {
                        patientRepository.findByIdOptional(patientVisit.patient.id).ifPresent(patient -> {
                            if (applyPatientVisitRequestSnapshotToPatient(patient, request)) {
                                patientRepository.persist(patient);
                            }
                        });
                    }

                    return new PatientVisitDTO(patientVisit);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    private boolean isHighestVisitNumberForPatient(PatientVisit patientVisit) {
        if (patientVisit.patient == null) {
            return false;
        }
        return patientVisitRepository
                .find("patient.id = ?1", Sort.descending("visitNumber"), patientVisit.patient.id)
                .firstResultOptional()
                .map(top -> top.visitNumber == patientVisit.visitNumber)
                .orElse(false);
    }

    /**
     * Applies the same snapshot fields as {@link #applyPatientVisitUpdateRequest} to {@link Patient}
     * (age, address, contact, occupation, next-of-kin). Does not touch visit-only fields.
     *
     * @return true if any patient field was updated
     */
    private boolean applyPatientVisitRequestSnapshotToPatient(Patient patient, PatientVisitUpdateRequest request) {
        if (request == null || patient == null) {
            return false;
        }
        boolean changed = false;
        if (request.patientAge != null) {
            patient.patientAge = request.patientAge;
            changed = true;
        }
        if (hasText(request.patientAddress)) {
            patient.patientAddress = request.patientAddress;
            changed = true;
        }
        if (hasText(request.patientContact)) {
            patient.patientContact = request.patientContact;
            changed = true;
        }
        if (hasText(request.occupation)) {
            patient.occupation = request.occupation;
            changed = true;
        }
        if (hasText(request.nextOfKinName)) {
            patient.nextOfKinName = request.nextOfKinName;
            changed = true;
        }
        if (hasText(request.nextOfKinContact)) {
            patient.nextOfKinContact = request.nextOfKinContact;
            changed = true;
        }
        if (hasText(request.relationship)) {
            patient.relationship = request.relationship;
            changed = true;
        }
        if (hasText(request.nextOfKinAddress)) {
            patient.nextOfKinAddress = request.nextOfKinAddress;
            changed = true;
        }
        if (changed) {
            patient.patientLastUpdatedDate = LocalDate.now();
        }
        return changed;
    }

    private void copyPatientSnapshotToVisit(PatientVisit patientVisit, Patient patient) {
        patientVisit.patientName = patient.patientFirstName + " " + patient.patientSecondName;
        patientVisit.patientAge = patient.patientAge;
        patientVisit.patientAddress = patient.patientAddress;
        patientVisit.patientContact = patient.patientContact;
        patientVisit.occupation = patient.occupation;
        patientVisit.nextOfKinName = patient.nextOfKinName;
        patientVisit.nextOfKinContact = patient.nextOfKinContact;
        patientVisit.relationship = patient.relationship;
        patientVisit.nextOfKinAddress = patient.nextOfKinAddress;
    }

    private void applyPatientVisitUpdateRequest(PatientVisit patientVisit, PatientVisitUpdateRequest request) {
        if (request == null) {
            return;
        }
        if (hasText(request.visitType)) {
            patientVisit.visitType = request.visitType;
        }
        if (hasText(request.visitReason)) {
            patientVisit.visitReason = request.visitReason;
        }
        if (request.goodsDelivered != null) {
            patientVisit.goodsDelivered = request.goodsDelivered;
        }
        if (request.deliveryNote != null) {
            patientVisit.deliveryNote = request.deliveryNote.isBlank() ? null : request.deliveryNote.trim();
        }
        applyVisitSnapshotFields(
                patientVisit,
                request.patientAge,
                request.patientAddress,
                request.patientContact,
                request.occupation,
                request.nextOfKinName,
                request.nextOfKinContact,
                request.relationship,
                request.nextOfKinAddress
        );
    }

    private void applyPatientVisitCreateRequest(PatientVisit patientVisit, PatientVisitRequest request) {
        if (request == null) {
            return;
        }
        applyVisitSnapshotFields(
                patientVisit,
                request.patientAge,
                request.patientAddress,
                request.patientContact,
                request.occupation,
                request.nextOfKinName,
                request.nextOfKinContact,
                request.relationship,
                request.nextOfKinAddress
        );
    }

    private void applyVisitSnapshotFields(
            PatientVisit patientVisit,
            BigDecimal patientAge,
            String patientAddress,
            String patientContact,
            String occupation,
            String nextOfKinName,
            String nextOfKinContact,
            String relationship,
            String nextOfKinAddress
    ) {
        if (patientAge != null) {
            patientVisit.patientAge = patientAge;
        }
        if (hasText(patientAddress)) {
            patientVisit.patientAddress = patientAddress;
        }
        if (hasText(patientContact)) {
            patientVisit.patientContact = patientContact;
        }
        if (hasText(occupation)) {
            patientVisit.occupation = occupation;
        }
        if (hasText(nextOfKinName)) {
            patientVisit.nextOfKinName = nextOfKinName;
        }
        if (hasText(nextOfKinContact)) {
            patientVisit.nextOfKinContact = nextOfKinContact;
        }
        if (hasText(relationship)) {
            patientVisit.relationship = relationship;
        }
        if (hasText(nextOfKinAddress)) {
            patientVisit.nextOfKinAddress = nextOfKinAddress;
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @Transactional
    public PatientVisitDTO updatePatientVisitStatusById(Long id, PatientVisitStatusUpdateRequest request) {
        return patientVisitRepository.findByIdOptional(id)
                .map(patientVisit -> {

                    if ("open".equalsIgnoreCase(request.visitStatus)
                            && !specialPrivilegeService.hasPrivilege(
                                    authenticatedUserResolver.requireCurrentUser(),
                                    SpecialPrivilegeService.Privilege.OPEN_CLOSED_VISIT)) {

                        throw new WebApplicationException(
                                Response.status(Response.Status.BAD_REQUEST)
                                        .entity(new ResponseMessage("You need admin approval to open a visit", null))
                                        .build()
                        );
                    }

                    patientVisit.visitStatus = request.visitStatus;
                    patientVisit.visitLastUpdatedDate = LocalDate.now();

                    patientVisitRepository.persist(patientVisit);

                    return new PatientVisitDTO(patientVisit);
                })
                .orElseThrow(() -> new WebApplicationException("Visit not found", 404));
    }

    private static boolean isAdminRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String normalized = role.trim().toLowerCase();
        return "md".equals(normalized)
                || "admin".equals(normalized)
                || "administrator".equals(normalized);
    }

    @Transactional
    public Response deletePatientVisitById(Long id, String userRole) {
        if (!isAdminRole(userRole)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("You need admin rights to delete a visit", null))
                    .build();
        }
        PatientVisit visit = patientVisitRepository.findById(id);
        if (visit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Visit not found", null))
                    .build();
        }
        patientVisitRepository.delete(visit);
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }













    /**
     * Only non-null / non-blank request fields add WHERE clauses; omitted params are ignored.
     * {@code procedureId} is ignored when null or {@code <= 0} (treat as “all services”).
     */
    private List<PatientVisit> runClinicalCriteriaQuery(VisitParametersRequest request) {
        if (request == null) {
            request = new VisitParametersRequest();
        }
        List<String> conditions = new ArrayList<>();
        List<Object> paramValues = new ArrayList<>();

        conditions.add("v.patient IS NOT NULL");

        Optional<Long> patientGroupId = resolveRequestPatientGroupId(request);
        if (patientGroupId.isPresent()) {
            paramValues.add(patientGroupId.get());
            conditions.add("p.patientGroup.id = ?" + paramValues.size());
        } else if (hasPatientGroupFilter(request)) {
            // Group was requested but could not be resolved — no rows match.
            conditions.add("1 = 0");
        }
        if (request.datefrom != null) {
            paramValues.add(request.datefrom);
            conditions.add("v.visitDate >= ?" + paramValues.size());
        }
        if (request.dateto != null) {
            paramValues.add(request.dateto);
            conditions.add("v.visitDate <= ?" + paramValues.size());
        }
        if (request.patientName != null && !request.patientName.isBlank()) {
            String pattern = "%" + request.patientName.trim().toLowerCase(Locale.ROOT) + "%";
            paramValues.add(pattern);
            int idx = paramValues.size();
            conditions.add(
                    "(LOWER(CONCAT(CONCAT(p.patientFirstName, ' '), p.patientSecondName)) LIKE ?" + idx
                            + " OR LOWER(COALESCE(v.patientName, '')) LIKE ?" + idx + ")");
        }
        if (request.diagnosis != null && !request.diagnosis.isBlank()) {
            String pattern = "%" + request.diagnosis.trim().toLowerCase(Locale.ROOT) + "%";
            paramValues.add(pattern);
            int idx = paramValues.size();
            conditions.add(
                    "EXISTS (SELECT 1 FROM Consultation c WHERE c.visit = v AND LOWER(COALESCE(c.diagnosis, '')) LIKE ?"
                            + idx + ")");
        }
        if (request.procedureId != null && request.procedureId > 0) {
            paramValues.add(request.procedureId);
            int idx = paramValues.size();
            conditions.add(
                    "EXISTS (SELECT 1 FROM ProcedureRequested pr WHERE pr.visit = v AND (pr.procedureId = ?" + idx
                            + " OR (pr.procedure IS NOT NULL AND pr.procedure.id = ?" + idx + ")))");
        }
        if (request.ageFrom != null) {
            paramValues.add(request.ageFrom);
            conditions.add("COALESCE(v.patientAge, p.patientAge) >= ?" + paramValues.size());
        }
        if (request.ageTo != null) {
            paramValues.add(request.ageTo);
            conditions.add("COALESCE(v.patientAge, p.patientAge) <= ?" + paramValues.size());
        }
        if (request.patientGender != null && !request.patientGender.isBlank()) {
            paramValues.add(request.patientGender.trim().toLowerCase(Locale.ROOT));
            conditions.add("LOWER(COALESCE(p.patientGender, '')) = ?" + paramValues.size());
        }
        if (request.weightFrom != null || request.weightTo != null) {
            List<String> weightParts = new ArrayList<>();
            weightParts.add("itv.visit = v");
            weightParts.add("itv.weight IS NOT NULL");
            if (request.weightFrom != null) {
                paramValues.add(request.weightFrom);
                weightParts.add("itv.weight >= ?" + paramValues.size());
            }
            if (request.weightTo != null) {
                paramValues.add(request.weightTo);
                weightParts.add("itv.weight <= ?" + paramValues.size());
            }
            conditions.add(
                    "EXISTS (SELECT 1 FROM InitialTriageVitals itv WHERE "
                            + String.join(" AND ", weightParts)
                            + ")");
        }
        if (request.treatmentItemId != null && request.treatmentItemId > 0) {
            paramValues.add(request.treatmentItemId);
            int idx = paramValues.size();
            conditions.add(
                    "EXISTS (SELECT 1 FROM TreatmentRequested tr WHERE tr.visit = v AND tr.itemId = ?" + idx + ")");
        }
        if (request.treatmentDrugName != null && !request.treatmentDrugName.isBlank()) {
            String pattern = "%" + request.treatmentDrugName.trim().toLowerCase(Locale.ROOT) + "%";
            paramValues.add(pattern);
            int idx = paramValues.size();
            conditions.add(
                    "EXISTS (SELECT 1 FROM TreatmentRequested tr WHERE tr.visit = v AND LOWER(COALESCE(tr.itemName, '')) LIKE ?"
                            + idx + ")");
        }
        if (Boolean.TRUE.equals(request.hasDebt)) {
            conditions.add("COALESCE(v.balanceDue, 0) > 0");
        } else if (Boolean.FALSE.equals(request.hasDebt)) {
            conditions.add("COALESCE(v.balanceDue, 0) <= 0");
        }

        String jpql = "SELECT v FROM PatientVisit v JOIN v.patient p WHERE "
                + String.join(" AND ", conditions)
                + " ORDER BY v.visitDate DESC, v.id DESC";

        Object[] args = paramValues.toArray(new Object[0]);
        return PatientVisit.find(jpql, args).list();
    }

    private boolean hasPatientGroupFilter(VisitParametersRequest request) {
        return request.patientGroupId != null && request.patientGroupId > 0
                || request.visitGroup != null && !request.visitGroup.isBlank();
    }

    /**
     * Resolve the registry patient group id from {@code patientGroupId} or legacy {@code visitGroup} text.
     */
    private Optional<Long> resolveRequestPatientGroupId(VisitParametersRequest request) {
        if (request.patientGroupId != null && request.patientGroupId > 0) {
            PatientGroup byId = patientGroupRepository.findById(request.patientGroupId);
            if (byId != null && byId.id != null) {
                return Optional.of(byId.id);
            }
            return Optional.of(request.patientGroupId);
        }
        if (request.visitGroup == null || request.visitGroup.isBlank()) {
            return Optional.empty();
        }
        String token = request.visitGroup.trim();
        PatientGroup resolved = patientGroupRepository.findByNormalizedShortForm(token);
        if (resolved == null) {
            resolved = patientGroupRepository.findByNormalizedGroupName(token);
        }
        if (resolved != null && resolved.id != null) {
            return Optional.of(resolved.id);
        }
        return Optional.empty();
    }

    private List<PatientVisit> resolveFilteredVisits(VisitParametersRequest request) {
        return runClinicalCriteriaQuery(request);
    }

    /**
     * Same filtering as {@link #runClinicalCriteriaQuery}; use inside a transaction when mapping to DTOs.
     */
    @Transactional
    public List<PatientVisit> filterVisitsByClinicalCriteria(VisitParametersRequest request) {
        return resolveFilteredVisits(request);
    }

    /**
     * Filtered visits as lightweight list DTOs (no nested collections / N+1 financial recomputes).
     * Needed so multi-month filters do not exhaust the DB connection.
     */
    @Transactional
    public List<PatientVisitDTO> filterVisitsByClinicalCriteriaAsDtos(VisitParametersRequest request) {
        List<PatientVisit> visits = resolveFilteredVisits(request);
        Map<Long, String> diagnoses = loadPrimaryDiagnosesByVisitId(visits);
        List<PatientVisitDTO> rows = new ArrayList<>(visits.size());
        for (PatientVisit visit : visits) {
            Long visitId = visit != null ? visit.id : null;
            rows.add(PatientVisitDTO.forListRow(visit, visitId != null ? diagnoses.get(visitId) : null));
        }
        return rows;
    }

    /** One query for all primary diagnoses instead of lazy-loading consultations per visit. */
    private Map<Long, String> loadPrimaryDiagnosesByVisitId(List<PatientVisit> visits) {
        Map<Long, String> out = new HashMap<>();
        if (visits == null || visits.isEmpty()) {
            return out;
        }
        List<Long> ids = visits.stream()
                .filter(Objects::nonNull)
                .map(v -> v.id)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return out;
        }
        final int chunkSize = 900;
        for (int i = 0; i < ids.size(); i += chunkSize) {
            List<Long> chunk = ids.subList(i, Math.min(i + chunkSize, ids.size()));
            @SuppressWarnings("unchecked")
            List<Object[]> rows = PatientVisit.getEntityManager()
                    .createQuery(
                            "SELECT c.visit.id, c.diagnosis FROM Consultation c "
                                    + "WHERE c.visit.id IN :ids ORDER BY c.id ASC")
                    .setParameter("ids", chunk)
                    .getResultList();
            for (Object[] row : rows) {
                if (row == null || row.length < 2 || row[0] == null) {
                    continue;
                }
                Long visitId = ((Number) row[0]).longValue();
                if (out.containsKey(visitId)) {
                    continue; // keep first (lowest id) diagnosis
                }
                if (row[1] != null) {
                    String diagnosis = String.valueOf(row[1]).trim();
                    if (!diagnosis.isEmpty()) {
                        out.put(visitId, diagnosis);
                    }
                }
            }
        }
        return out;
    }

    private FullVisitResponse from(Row row){

        FullVisitResponse response = new FullVisitResponse();
        response.id = row.getLong("id");
        response.patientId = row.getLong("patient_id");
        response.visitName = row.getString("visitName");
        response.visitNumber = row.getInteger("visitNumber");
        response.visitReason = row.getString("visitReason");
        response.visitType = row.getString("visitType");
        response.visitStatus = row.getString("visitStatus");
        response.visitDate = row.getLocalDate("visitDate");
        response.visitLastUpdatedDate = row.getLocalDate("visitLastUpdatedDate");
        response.visitTime = row.getLocalTime("visitTime");
        response.subTotal = row.getBigDecimal("subTotal");
        response.totalAmount = row.getBigDecimal("totalAmount");

        response.amountPaid = row.getBigDecimal("amountPaid");
        response.balanceDue = row.getBigDecimal("balanceDue");
        response.visitGroup = row.getString("visitGroup");



        return response;
    }

    @Transactional
    public PatientVisitDebtResult calculateFinancialTotals(List<PatientVisit> filteredVisits) {

        // Calculate only the requested totals
        BigDecimal totalBalanceDue = filteredVisits.stream()
                .map(PatientVisit::getBalanceDue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmountPaid = filteredVisits.stream()
                .map(PatientVisit::getAmountPaid)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = filteredVisits.stream()
                .map(PatientVisit::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);



        List<PatientVisitDTO> patientVisitDTOs = filteredVisits.stream()
                .map(PatientVisitDTO::new)
                .collect(Collectors.toList());

        return new PatientVisitDebtResult(
                patientVisitDTOs,
                totalBalanceDue,
                totalAmountPaid,
                totalAmount
                  // Add patient names to the result
        );
    }

    // Updated wrapper record with patient names
    public record PatientVisitDebtResult(
            List<PatientVisitDTO> patientVisits,
            BigDecimal totalBalanceDue,
            BigDecimal totalAmountPaid,
            BigDecimal totalAmount
    ) {}

    @Transactional
    public Response generateAndReturnCompassionGroupInvoiceDocx(VisitParametersRequest request) {
        List<PatientVisit> filteredVisits = resolveFilteredVisits(request);
        return compassionGroupInvoiceDocxService.generateDocx(request, filteredVisits);
    }

    @Transactional
    public Response generateAndReturnInvoicePdfForListOfCompassionPatients(VisitParametersRequest request) {
        try {
            List<PatientVisit> filteredVisits = resolveFilteredVisits(request);

            calculateFinancialTotals(filteredVisits);

            PatientVisitDebtResult result = calculateFinancialTotals(filteredVisits);

// Extract the data from the result
            List<PatientVisitDTO> patientVisitDTOs = result.patientVisits();
            BigDecimal totalBalanceDue = result.totalBalanceDue();
            BigDecimal totalAmountPaid = result.totalAmountPaid();
            BigDecimal totalAmount = result.totalAmount();

            String title = (totalAmountPaid != null && totalAmountPaid.compareTo(BigDecimal.ZERO) > 0)
                    ? "RECEIPT"
                    : "INVOICE";

            // Create the PDF document
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHelperInvoice(facilityBranding()));

            Document document = new Document(pdfDocument);
            document.setMargins(36, 36, 90, 36);



            // Add invoice title
            Table invoiceTitle = new Table(new float[]{1});
            invoiceTitle.setWidth(UnitValue.createPercentValue(100));
            invoiceTitle.addCell(new Cell()
                    .add(new Div()
                            .setBorderBottom(new SolidBorder(1)) // Underline (1px solid line)
                            .setPaddingBottom(2) // Space between text and underline
                            .add(new Paragraph(title)
                                    .setBold()
                                    .setFontSize(11)
                                    .setTextAlignment(TextAlignment.CENTER)
                            )
                    )
                    .add(new Paragraph(facilityBranding().financeHeaderLine())
                            .setFontSize(7)
                            //.setItalic()
                            .setMarginTop(3)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(15)
            );
            document.add(invoiceTitle);

            // Add header: Logo and Invoice Details
            Table headerTable = new Table(new float[]{1, 1, 1, 2, 1});
            headerTable.setWidth(UnitValue.createPercentValue(100));

            // Add logo
            headerTable.addCell(new Cell()
                    .add(getLogo().setWidth(79).setHeight(68))
                    .setBorder(Border.NO_BORDER)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT)
                    .setVerticalAlignment(VerticalAlignment.TOP)
                    .setPaddingTop(-7)
                    .setPaddingLeft(-22)
            );

            // Add invoice details
            headerTable.addCell(new Cell()
                    .add(new Paragraph("FROM: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("ADDRESS: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("TO: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("ADDRESS: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("VENERANDA MEDICAL").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("BUGOGO TOWN COUNCIL, KYEGEGWA DISTRICT").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("KATOMA CHILD AND YOUTH DEVELOPMENT CENTER")
                            .setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph()
                            .add(Optional.of("KATOMA - BUGOGO TOWN COUNCIL, KYEGEGWA DISTRICT")
                                    .map(String::toUpperCase)
                                    .orElse(""))
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT)
                    )


                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("NUMBER: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("DUE DATE RANGE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("DATE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("BALANCE DUE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("UG-5003").setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(request.datefrom +" - "+ request.dateto)).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(LocalDate.now())).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf("UGX: "+totalBalanceDue)).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))

                    .setBorder(Border.NO_BORDER)
            );

            document.add(headerTable);

            // Add items table
            float[] columnWidths = {4, 1, 2, 2, 2};
            Table itemsTable = new Table(columnWidths);
            itemsTable.setWidth(UnitValue.createPercentValue(100));

            // Add header with no column lines
            itemsTable.addCell(createCell("CLIENT NAME", 1, TextAlignment.LEFT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("SERVICE", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("AMOUNT TO PAY (UGX)", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("AMOUNT PAID (UGX)", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("BALANCE DUE (UGX)", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLACK)
                    .setBorder(Border.NO_BORDER));



            boolean isEvenRow = false;
// Calculate the totals



            for (PatientVisitDTO patientVisitDto : patientVisitDTOs) {
                com.itextpdf.kernel.colors.Color rowColor = isEvenRow
                        ? ColorConstants.WHITE
                        : ColorConstants.LIGHT_GRAY;

                String patientName = patientVisitDto.patientName != null ? patientVisitDto.patientName.toUpperCase() : "UNKNOWN PATIENT";

                itemsTable.addCell(createCell(patientName, 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf("MEDICAL BILLS"), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(patientVisitDto.totalAmount), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(patientVisitDto.amountPaid), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(patientVisitDto.balanceDue), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                isEvenRow = !isEvenRow;
            }



            document.add(itemsTable);



            // Add totals table
            Table totalsTable = new Table(new float[]{4, 2, 2});
            totalsTable.setWidth(UnitValue.createPercentValue(100));

            //PatientGroupDTO patientDTO = patientGroupService.getPatientGroupById(1);


            Cell notesCell1 = new Cell(6, 1)
                    .add(new Paragraph("\n IMPRESSION / DIAGNOSIS: " ))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.TOP);
            totalsTable.addCell(notesCell1);

            //BigDecimal totalDebt = result.totalBalanceDue();

            // Add discount row
            totalsTable.addCell(createCell("DISCOUNT:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell(createCell("0", 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            // Add tax row
            totalsTable.addCell(createCell("TAX:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell("0", 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));

            // Add total amount row
            totalsTable.addCell(createCell("TOTAL AMOUNT:", 1, TextAlignment.LEFT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell(createCell(String.valueOf(totalAmount), 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            // Add amount paid row
            totalsTable.addCell(createCell("AMOUNT PAID:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell(String.valueOf(totalAmountPaid), 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));

            // Add balance due row
            totalsTable.addCell(createCell("BALANCE DUE:", 1, TextAlignment.LEFT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell(createCell(String.valueOf(totalBalanceDue), 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            document.add(totalsTable);

            // Close the document
            document.close();

            // Return the PDF as a response
            byte[] pdfBytes = baos.toByteArray();
            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=invoice.pdf")
                    .type("application/pdf")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Utility method to create cells with alignment
    private Cell createCell(String content, int i, TextAlignment alignment) {
        Cell cell = new Cell().add(new Paragraph(content));
        cell.setTextAlignment(alignment);
        return cell;
    }

    private Image getLogo() {
        return facilityPdfLogoService.createLogoImage();
    }





































}






