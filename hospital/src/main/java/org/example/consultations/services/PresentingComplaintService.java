package org.example.consultations.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.Consultation;
import org.example.consultations.domains.ConsultationRepository;
import org.example.consultations.domains.PresentingComplaint;
import org.example.consultations.domains.PresentingComplaintRepository;
import org.example.consultations.services.payloads.requests.PresentingComplaintRequest;
import org.example.consultations.services.payloads.responses.PresentingComplaintDTO;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class PresentingComplaintService {

    @Inject
    PresentingComplaintRepository presentingComplaintRepository;

    @Inject
    ConsultationRepository consultationRepository;

    @Transactional
    public Response createForVisit(Long visitId, PresentingComplaintRequest request) {
        if (request == null || blankToNull(request.complaint) == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Presenting complaint is required", null))
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
                    .entity(new ResponseMessage("Visit is closed. You cannot add presenting complaints.", null))
                    .build();
        }

        Consultation consultation = ensureConsultation(visit);
        PresentingComplaint row = new PresentingComplaint();
        row.consultation = consultation;
        applyRequest(row, request);
        row.creationDate = LocalDate.now();
        row.updateDate = LocalDate.now();

        if (consultation.presentingComplaints == null) {
            consultation.presentingComplaints = new java.util.ArrayList<>();
        }
        consultation.presentingComplaints.add(row);
        presentingComplaintRepository.persist(row);
        syncLegacyChiefComplaint(consultation);

        return Response.ok(new ResponseMessage("Presenting complaint created successfully", new PresentingComplaintDTO(row))).build();
    }

    @Transactional
    public Response update(Long id, PresentingComplaintRequest request) {
        PresentingComplaint row = presentingComplaintRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Presenting complaint not found", null))
                    .build();
        }
        if (request == null || blankToNull(request.complaint) == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Presenting complaint is required", null))
                    .build();
        }

        Consultation consultation = row.consultation;
        if (consultation != null && consultation.visit != null && "closed".equals(consultation.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot update presenting complaints.", null))
                    .build();
        }

        applyRequest(row, request);
        row.updateDate = LocalDate.now();
        syncLegacyChiefComplaint(consultation);

        return Response.ok(new ResponseMessage("Presenting complaint updated successfully", new PresentingComplaintDTO(row))).build();
    }

    @Transactional
    public Response delete(Long id) {
        PresentingComplaint row = presentingComplaintRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Presenting complaint not found", null))
                    .build();
        }

        Consultation consultation = row.consultation;
        if (consultation != null && consultation.visit != null && "closed".equals(consultation.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot delete presenting complaints.", null))
                    .build();
        }

        if (consultation != null && consultation.presentingComplaints != null) {
            consultation.presentingComplaints.remove(row);
        }
        presentingComplaintRepository.delete(row);
        syncLegacyChiefComplaint(consultation);

        return Response.ok(new ResponseMessage("Presenting complaint deleted successfully", null)).build();
    }

    @Transactional
    public List<PresentingComplaintDTO> listByVisitId(Long visitId) {
        return presentingComplaintRepository
                .list("consultation.visit.id", Sort.ascending("id"), visitId)
                .stream()
                .map(PresentingComplaintDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response getById(Long id) {
        PresentingComplaint row = presentingComplaintRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Presenting complaint not found", null))
                    .build();
        }
        return Response.ok(new ResponseMessage("Presenting complaint fetched successfully", new PresentingComplaintDTO(row))).build();
    }

    private void applyRequest(PresentingComplaint row, PresentingComplaintRequest request) {
        row.complaint = Objects.requireNonNull(blankToNull(request.complaint));
        row.site = blankToNull(request.site);
        row.severity = blankToNull(request.severity);
        row.onset = blankToNull(request.onset);
        row.durationValue = request.durationValue != null && request.durationValue > 0 ? request.durationValue : null;
        row.durationUnit = normalizeDurationUnit(request.durationUnit);
        row.duration = formatDuration(row.durationValue, row.durationUnit);
        row.nature = blankToNull(request.nature);
        row.course = blankToNull(request.course);
        row.aggravatingFactors = blankToNull(request.aggravatingFactors);
        row.alleviatingFactors = blankToNull(request.alleviatingFactors);
        row.associatedSymptoms = blankToNull(request.associatedSymptoms);
    }

    private static String normalizeDurationUnit(String unit) {
        String value = blankToNull(unit);
        if (value == null) {
            return null;
        }
        String normalized = value.toLowerCase().trim();
        if (normalized.startsWith("day")) {
            return "day";
        }
        if (normalized.startsWith("week")) {
            return "week";
        }
        if (normalized.startsWith("month")) {
            return "month";
        }
        if (normalized.startsWith("year")) {
            return "year";
        }
        return null;
    }

    private static String formatDuration(Integer value, String unit) {
        if (value == null || value <= 0 || unit == null || unit.isBlank()) {
            return null;
        }
        String label = unit;
        if (value != 1) {
            label = unit + "s";
        }
        return value + " " + label;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
        consultationRepository.persist(consultation);
        return consultation;
    }

    /** Keep legacy Consultation.chiefComplaint text in sync for PDFs / old UIs. */
    private void syncLegacyChiefComplaint(Consultation consultation) {
        if (consultation == null) {
            return;
        }
        if (consultation.presentingComplaints == null || consultation.presentingComplaints.isEmpty()) {
            consultation.chiefComplaint = null;
            consultation.updateDate = LocalDate.now();
            return;
        }
        String joined = consultation.presentingComplaints.stream()
                .map(pc -> pc != null ? pc.complaint : null)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("; "));
        consultation.chiefComplaint = joined.isEmpty() ? null : joined;
        consultation.updateDate = LocalDate.now();
    }
}
