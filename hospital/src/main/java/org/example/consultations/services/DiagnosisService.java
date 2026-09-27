package org.example.consultations.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.Consultation;
import org.example.consultations.domains.ConsultationRepository;
import org.example.consultations.domains.Diagnosis;
import org.example.consultations.domains.DiagnosisRepository;
import org.example.consultations.domains.DiagnosisType;
import org.example.consultations.domains.DiagnosisTypeRepository;
import org.example.consultations.services.payloads.requests.DiagnosisRequest;
import org.example.consultations.services.payloads.responses.DiagnosisDTO;
import org.example.treatment.domains.TreatmentRequested;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class DiagnosisService {

    @Inject
    DiagnosisRepository diagnosisRepository;

    @Inject
    DiagnosisTypeRepository diagnosisTypeRepository;

    @Inject
    ConsultationRepository consultationRepository;

    @Transactional
    public Response createDiagnosisForVisit(Long visitId, DiagnosisRequest request) {
        if (request == null || request.name == null || request.name.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Diagnosis name is required", null))
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
                    .entity(new ResponseMessage("Visit is closed. You cannot add diagnoses.", null))
                    .build();
        }

        Consultation consultation = ensureConsultation(visit);
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.consultation = consultation;
        diagnosis.name = request.name.trim();
        diagnosis.severity = blankToNull(request.severity);
        diagnosis.kind = normalizeKind(request.kind);
        diagnosis.notes = blankToNull(request.notes);
        applyHmisFields(diagnosis, request);
        diagnosis.creationDate = LocalDate.now();
        diagnosis.updateDate = LocalDate.now();

        if (consultation.diagnoses == null) {
            consultation.diagnoses = new java.util.ArrayList<>();
        }
        consultation.diagnoses.add(diagnosis);
        diagnosisRepository.persist(diagnosis);
        syncLegacyDiagnosisText(consultation);

        return Response.ok(new ResponseMessage("Diagnosis created successfully", new DiagnosisDTO(diagnosis))).build();
    }

    @Transactional
    public Response updateDiagnosis(Long diagnosisId, DiagnosisRequest request) {
        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId);
        if (diagnosis == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Diagnosis not found", null))
                    .build();
        }
        if (request == null || request.name == null || request.name.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Diagnosis name is required", null))
                    .build();
        }

        Consultation consultation = diagnosis.consultation;
        if (consultation != null && consultation.visit != null && "closed".equals(consultation.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot update diagnoses.", null))
                    .build();
        }

        diagnosis.name = request.name.trim();
        diagnosis.severity = blankToNull(request.severity);
        diagnosis.kind = normalizeKind(request.kind);
        diagnosis.notes = blankToNull(request.notes);
        applyHmisFields(diagnosis, request);
        diagnosis.updateDate = LocalDate.now();
        syncLegacyDiagnosisText(consultation);

        return Response.ok(new ResponseMessage("Diagnosis updated successfully", new DiagnosisDTO(diagnosis))).build();
    }

    @Transactional
    public Response deleteDiagnosis(Long diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId);
        if (diagnosis == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Diagnosis not found", null))
                    .build();
        }

        Consultation consultation = diagnosis.consultation;
        if (consultation != null && consultation.visit != null && "closed".equals(consultation.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot delete diagnoses.", null))
                    .build();
        }

        // Keep treatments on the visit; only unlink clinical diagnosis
        if (diagnosis.treatments != null) {
            for (TreatmentRequested treatment : diagnosis.treatments) {
                treatment.diagnosis = null;
            }
            diagnosis.treatments.clear();
        }

        if (consultation != null && consultation.diagnoses != null) {
            consultation.diagnoses.remove(diagnosis);
        }
        diagnosisRepository.delete(diagnosis);
        syncLegacyDiagnosisText(consultation);

        return Response.ok(new ResponseMessage("Diagnosis deleted successfully", null)).build();
    }

    @Transactional
    public List<DiagnosisDTO> listByVisitId(Long visitId) {
        return diagnosisRepository
                .list("consultation.visit.id", Sort.ascending("id"), visitId)
                .stream()
                .map(DiagnosisDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<DiagnosisDTO> listByConsultationId(Long consultationId) {
        return diagnosisRepository
                .list("consultation.id", Sort.ascending("id"), consultationId)
                .stream()
                .map(DiagnosisDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response getById(Long diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId);
        if (diagnosis == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Diagnosis not found", null))
                    .build();
        }
        return Response.ok(new ResponseMessage("Diagnosis fetched successfully", new DiagnosisDTO(diagnosis))).build();
    }

    private void applyHmisFields(Diagnosis diagnosis, DiagnosisRequest request) {
        if (request == null || diagnosis == null) {
            return;
        }
        if (request.diagnosisTypeId != null) {
            DiagnosisType type = diagnosisTypeRepository.findById(request.diagnosisTypeId);
            if (type != null) {
                diagnosis.diagnosisType = type;
                if (request.hmisCode == null || request.hmisCode.isBlank()) {
                    diagnosis.hmisCode = blankToNull(type.hmisCode);
                }
                if (request.icd10Code == null || request.icd10Code.isBlank()) {
                    diagnosis.icd10Code = blankToNull(type.icd10Code);
                }
            }
        }
        if (request.hmisCode != null) {
            diagnosis.hmisCode = blankToNull(request.hmisCode);
        }
        if (request.icd10Code != null) {
            diagnosis.icd10Code = blankToNull(request.icd10Code);
        }
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
        consultationRepository.persist(consultation);
        return consultation;
    }

    /** Keep legacy Consultation.diagnosis text in sync for PDFs / old UIs. */
    private void syncLegacyDiagnosisText(Consultation consultation) {
        if (consultation == null) {
            return;
        }
        if (consultation.diagnoses == null || consultation.diagnoses.isEmpty()) {
            return;
        }
        String joined = consultation.diagnoses.stream()
                .map(DiagnosisService::formatDiagnosisStatement)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
        if (!joined.isEmpty()) {
            consultation.diagnosis = joined;
            consultation.updateDate = LocalDate.now();
        }
    }

    /** Statement form: severity first, then diagnosis name (e.g. "Severe Pneumonia"). */
    public static String formatDiagnosisStatement(Diagnosis diagnosis) {
        if (diagnosis == null) {
            return null;
        }
        String severity = diagnosis.severity != null ? diagnosis.severity.trim() : "";
        String name = diagnosis.name != null ? diagnosis.name.trim() : "";
        if (!severity.isEmpty() && !name.isEmpty()) {
            return severity + " " + name;
        }
        if (!name.isEmpty()) {
            return name;
        }
        return severity.isEmpty() ? null : severity;
    }

    private static String normalizeKind(String kind) {
        if (kind == null || kind.trim().isEmpty()) {
            return "final";
        }
        String value = kind.trim().toLowerCase();
        if ("differential".equals(value) || "diff".equals(value)) {
            return "differential";
        }
        return "final";
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
