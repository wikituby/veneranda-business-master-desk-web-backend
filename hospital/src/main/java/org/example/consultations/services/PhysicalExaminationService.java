package org.example.consultations.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.Consultation;
import org.example.consultations.domains.ConsultationRepository;
import org.example.consultations.domains.PhysicalExamination;
import org.example.consultations.domains.PhysicalExaminationRepository;
import org.example.consultations.services.payloads.requests.PhysicalExaminationRequest;
import org.example.consultations.services.payloads.responses.PhysicalExaminationDTO;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class PhysicalExaminationService {

    @Inject
    PhysicalExaminationRepository physicalExaminationRepository;

    @Inject
    ConsultationRepository consultationRepository;

    @Transactional
    public Response createForVisit(Long visitId, PhysicalExaminationRequest request) {
        if (request == null || blankToNull(request.system) == null || blankToNull(request.findings) == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("System and findings are required", null))
                    .build();
        }

        PatientVisit visit = PatientVisit.findById(visitId);
        if (visit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Visit not found", null))
                    .build();
        }
        if ("closed".equals(visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot add physical examinations.", null))
                    .build();
        }

        Consultation consultation = ensureConsultation(visit);
        PhysicalExamination row = new PhysicalExamination();
        row.consultation = consultation;
        applyRequest(row, request);
        row.creationDate = LocalDate.now();
        row.updateDate = LocalDate.now();

        if (consultation.physicalExaminations == null) {
            consultation.physicalExaminations = new java.util.ArrayList<>();
        }
        consultation.physicalExaminations.add(row);
        physicalExaminationRepository.persist(row);
        syncLegacyExaminationText(consultation);

        return Response.ok(new ResponseMessage("Physical examination created successfully", new PhysicalExaminationDTO(row))).build();
    }

    @Transactional
    public Response update(Long id, PhysicalExaminationRequest request) {
        PhysicalExamination row = physicalExaminationRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Physical examination not found", null))
                    .build();
        }
        if (request == null || blankToNull(request.system) == null || blankToNull(request.findings) == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("System and findings are required", null))
                    .build();
        }

        Consultation consultation = row.consultation;
        if (consultation != null && consultation.visit != null && "closed".equals(consultation.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot update physical examinations.", null))
                    .build();
        }

        applyRequest(row, request);
        row.updateDate = LocalDate.now();
        syncLegacyExaminationText(consultation);

        return Response.ok(new ResponseMessage("Physical examination updated successfully", new PhysicalExaminationDTO(row))).build();
    }

    @Transactional
    public Response delete(Long id) {
        PhysicalExamination row = physicalExaminationRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Physical examination not found", null))
                    .build();
        }

        Consultation consultation = row.consultation;
        if (consultation != null && consultation.visit != null && "closed".equals(consultation.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot delete physical examinations.", null))
                    .build();
        }

        if (consultation != null && consultation.physicalExaminations != null) {
            consultation.physicalExaminations.remove(row);
        }
        physicalExaminationRepository.delete(row);
        syncLegacyExaminationText(consultation);

        return Response.ok(new ResponseMessage("Physical examination deleted successfully", null)).build();
    }

    @Transactional
    public List<PhysicalExaminationDTO> listByVisitId(Long visitId) {
        return physicalExaminationRepository
                .list("consultation.visit.id", Sort.ascending("id"), visitId)
                .stream()
                .map(PhysicalExaminationDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response getById(Long id) {
        PhysicalExamination row = physicalExaminationRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Physical examination not found", null))
                    .build();
        }
        return Response.ok(new ResponseMessage("Physical examination fetched successfully", new PhysicalExaminationDTO(row))).build();
    }

    private void applyRequest(PhysicalExamination row, PhysicalExaminationRequest request) {
        row.examSystem = Objects.requireNonNull(blankToNull(request.system));
        row.findings = Objects.requireNonNull(blankToNull(request.findings));
        row.site = blankToNull(request.site);
        row.status = blankToNull(request.status);
        row.inspection = blankToNull(request.inspection);
        row.palpation = blankToNull(request.palpation);
        row.percussion = blankToNull(request.percussion);
        row.auscultation = blankToNull(request.auscultation);
        row.notes = blankToNull(request.notes);
    }

    private Consultation ensureConsultation(PatientVisit visit) {
        Consultation consultation = consultationRepository.find("visit.id", visit.id).firstResult();
        if (consultation != null) {
            return consultation;
        }
        consultation = new Consultation();
        consultation.visit = visit;
        consultation.creationDate = LocalDate.now();
        consultation.updateDate = LocalDate.now();
        consultation.diagnoses = new java.util.ArrayList<>();
        consultation.presentingComplaints = new java.util.ArrayList<>();
        consultation.physicalExaminations = new java.util.ArrayList<>();
        consultationRepository.persist(consultation);
        return consultation;
    }

    /** Keep legacy examination TEXT fields in sync for PDFs / old UIs. */
    private void syncLegacyExaminationText(Consultation consultation) {
        if (consultation == null) {
            return;
        }
        if (consultation.physicalExaminations == null || consultation.physicalExaminations.isEmpty()) {
            consultation.clinicalExamination = null;
            consultation.updateDate = LocalDate.now();
            return;
        }

        String joined = consultation.physicalExaminations.stream()
                .filter(Objects::nonNull)
                .map(pe -> {
                    String system = pe.examSystem != null ? pe.examSystem.trim() : "";
                    String findings = pe.findings != null ? pe.findings.trim() : "";
                    if (system.isEmpty() && findings.isEmpty()) {
                        return null;
                    }
                    if (system.isEmpty()) {
                        return findings;
                    }
                    if (findings.isEmpty()) {
                        return system;
                    }
                    return system + ": " + findings;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));
        consultation.clinicalExamination = joined.isEmpty() ? null : joined;

        // Also map into system-specific legacy columns when possible
        consultation.systemicExamination = findFindingsBySystem(consultation, "general", "systemic");
        consultation.respiratoryExamination = findFindingsBySystem(consultation, "respiratory", "chest", "lungs");
        consultation.cardiovascularExamination = findFindingsBySystem(consultation, "cardiovascular", "cardiac", "heart", "cvs");
        consultation.cnsExamination = findFindingsBySystem(consultation, "cns", "neurological", "neuro");
        consultation.abdominalExamination = findFindingsBySystem(consultation, "abdominal", "abdomen", "git");
        consultation.musculoskeletalExamination = findFindingsBySystem(consultation, "musculoskeletal", "msk", "orthopedic", "orthopaedic");

        consultation.updateDate = LocalDate.now();
    }

    private static String findFindingsBySystem(Consultation consultation, String... keywords) {
        if (consultation.physicalExaminations == null) {
            return null;
        }
        return consultation.physicalExaminations.stream()
                .filter(Objects::nonNull)
                .filter(pe -> pe.examSystem != null && matchesSystem(pe.examSystem, keywords))
                .map(pe -> pe.findings)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("; "));
    }

    private static boolean matchesSystem(String system, String... keywords) {
        String value = system.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (value.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
