package org.example.consultations.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.Complaint;
import org.example.consultations.domains.ComplaintRepository;
import org.example.consultations.domains.ComplaintSite;
import org.example.consultations.domains.ComplaintSiteRepository;
import org.example.consultations.domains.ComplaintType;
import org.example.consultations.domains.ComplaintTypeRepository;
import org.example.consultations.domains.Consultation;
import org.example.consultations.domains.ConsultationDocument;
import org.example.consultations.domains.ConsultationDocumentRepository;
import org.example.consultations.domains.ConsultationRepository;
import org.example.consultations.services.payloads.requests.ComplaintRequest;
import org.example.consultations.services.payloads.requests.ConsultationDocumentRequest;
import org.example.consultations.services.payloads.requests.ConsultationRequest;
import org.example.consultations.services.payloads.responses.ConsultationDTO;
import org.example.consultations.services.payloads.responses.ConsultationDocumentDTO;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ConsultationService {

    @Inject
    ConsultationRepository consultationRepository;

    @Inject
    ComplaintRepository complaintRepository;

    @Inject
    ComplaintTypeRepository complaintTypeRepository;

    @Inject
    ComplaintSiteRepository complaintSiteRepository;

    @Inject
    ConsultationDocumentRepository consultationDocumentRepository;


    private void processComplaints(Consultation consultation, List<ComplaintRequest> complaintRequests) {
        if (complaintRequests == null || complaintRequests.isEmpty()) {
            return;
        }

        // Clear existing complaints if updating
        if (consultation.complaints != null && !consultation.complaints.isEmpty()) {
            consultation.complaints.clear();
        }

        // Create new complaints from request
        for (ComplaintRequest complaintRequest : complaintRequests) {
            Complaint complaint = new Complaint();
            complaint.consultation = consultation;
            
            // Fetch ComplaintSite by ID
            if (complaintRequest.siteId != null) {
                ComplaintSite complaintSite = complaintSiteRepository.findById(complaintRequest.siteId);
                if (complaintSite == null) {
                    throw new WebApplicationException("ComplaintSite with id " + complaintRequest.siteId + " not found", Response.Status.BAD_REQUEST);
                }
                complaint.site = complaintSite;
            } else {
                throw new WebApplicationException("ComplaintSite ID is required", Response.Status.BAD_REQUEST);
            }
            
            // Fetch ComplaintType by ID
            if (complaintRequest.typeId != null) {
                ComplaintType complaintType = complaintTypeRepository.findById(complaintRequest.typeId);
                if (complaintType == null) {
                    throw new WebApplicationException("ComplaintType with id " + complaintRequest.typeId + " not found", Response.Status.BAD_REQUEST);
                }
                complaint.type = complaintType;
            } else {
                throw new WebApplicationException("ComplaintType ID is required", Response.Status.BAD_REQUEST);
            }
            
            complaint.duration = complaintRequest.duration;
            complaint.natureCharacter = complaintRequest.natureCharacter;
            complaint.severity = complaintRequest.severity;
            complaint.onset = complaintRequest.onset;
            complaint.courseProgression = complaintRequest.courseProgression;
            complaint.aggravatingFactors = complaintRequest.aggravatingFactors;
            complaint.relievingFactors = complaintRequest.relievingFactors;
            complaint.associatedSymptoms = complaintRequest.associatedSymptoms;
            complaint.creationDate = LocalDate.now();
            
            consultation.complaints.add(complaint);
        }
    }

    @Transactional
    public Response createNewConsultation(Long visitId, ConsultationRequest request) {
        // Fetch the patient visit by ID
        PatientVisit patientVisit = PatientVisit.findById(visitId);

        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }

        Consultation consultation = consultationRepository.find("visit.id", visitId).firstResult();
        if (consultation == null) {
            consultation = new Consultation();
            consultation.creationDate = LocalDate.now();
        } else {
            consultation.updateDate = LocalDate.now();
        }

        consultation.visit = patientVisit;
        if (hasText(request.doneBy)) {
            consultation.doneBy = request.doneBy;
        }
        if (hasText(request.historyOfPresentingComplaint)) {
            consultation.historyOfPresentingComplaint = request.historyOfPresentingComplaint;
        }
        if (hasText(request.chiefComplaint)) {
            consultation.chiefComplaint = request.chiefComplaint;
        }
        if (hasText(request.report)) {
            consultation.report = request.report;
        }
        if (hasText(request.allergies)) {
            consultation.allergies = request.allergies;
        }
        if (hasText(request.familyHistory)) {
            consultation.familyHistory = request.familyHistory;
        }
        if (hasText(request.socialHistory)) {
            consultation.socialHistory = request.socialHistory;
        }
        if (hasText(request.pastObstetricHistory)) {
            consultation.pastObstetricHistory = request.pastObstetricHistory;
        }
        if (hasText(request.pastGynaecologicalHistory)) {
            consultation.pastGynaecologicalHistory = request.pastGynaecologicalHistory;
        }
        if (hasText(request.systemicExamination)) {
            consultation.systemicExamination = request.systemicExamination;
        }
        if (hasText(request.respiratoryExamination)) {
            consultation.respiratoryExamination = request.respiratoryExamination;
        }
        if (hasText(request.cardiovascularExamination)) {
            consultation.cardiovascularExamination = request.cardiovascularExamination;
        }
        if (hasText(request.cnsExamination)) {
            consultation.cnsExamination = request.cnsExamination;
        }
        if (hasText(request.abdominalExamination)) {
            consultation.abdominalExamination = request.abdominalExamination;
        }
        if (hasText(request.musculoskeletalExamination)) {
            consultation.musculoskeletalExamination = request.musculoskeletalExamination;
        }
        if (hasText(request.reviewOfOtherSystems)) {
            consultation.reviewOfOtherSystems = request.reviewOfOtherSystems;
        }
        if (hasText(request.clinicalImpression)) {
            consultation.clinicalImpression = request.clinicalImpression;
        }
        if (hasText(request.followUpInstructions)) {
            consultation.followUpInstructions = request.followUpInstructions;
        }
        if (hasText(request.notes)) {
            consultation.notes = request.notes;
        }
        if (hasText(request.medicationHistory)) {
            consultation.medicationHistory = request.medicationHistory;
        }
        if (hasText(request.clinicalExamination)) {
            consultation.clinicalExamination = request.clinicalExamination;
        }
        if (hasText(request.differentialDiagnosis)) {
            consultation.differentialDiagnosis = request.differentialDiagnosis;
        }
        if (hasText(request.diagnosis)) {
            consultation.diagnosis = request.diagnosis;
        }
        if (hasText(request.medicalHistory)) {
            consultation.medicalHistory = request.medicalHistory;
        }

        // Process complaints
        //processComplaints(consultation, request.complaints);

        // Save consultation
        consultationRepository.persist(consultation);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patient consultation saved successfully", new ConsultationDTO(consultation)))
                .build();
    }

    @Transactional
    public ConsultationDTO newConsultationOnTheGo(Long visitId){

        PatientVisit patientVisit = PatientVisit.findById(visitId);

        if ("closed".equals(patientVisit.visitStatus)) {
            throw new WebApplicationException("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848:",409);

        }


        Consultation consultation = new Consultation();


        consultation.visit = patientVisit;
        consultation.chiefComplaint = "";
        consultation.doneBy = "";
        consultation.historyOfPresentingComplaint = "";
        consultation.medicationHistory = "";
        consultation.allergies = "";
        consultation.familyHistory = "";
        consultation.socialHistory = "";
        consultation.pastObstetricHistory = "";
        consultation.pastGynaecologicalHistory = "";
        consultation.systemicExamination = "";
        consultation.respiratoryExamination = "";
        consultation.cardiovascularExamination = "";
        consultation.cnsExamination = "";
        consultation.abdominalExamination = "";
        consultation.musculoskeletalExamination = "";
        consultation.reviewOfOtherSystems = "";
        consultation.clinicalImpression = "";
        consultation.followUpInstructions = "";
        consultation.notes = "";
        consultation.report = "";
        consultation.clinicalExamination = "";
        consultation.differentialDiagnosis = "";
        consultation.diagnosis = "";
        consultation.creationDate = LocalDate.now();
        consultation.medicalHistory = "";


        consultationRepository.persist(consultation);

        return new ConsultationDTO(consultation);

    }




    @Transactional
    public ConsultationDTO getFirstConsultationByVisitId(Long visitId) {
        Consultation consultationNew = Consultation.find(
                "visit.id = ?1 ORDER BY id DESC", visitId
        ).firstResult();


        if (consultationNew == null) {

            return newConsultationOnTheGo(visitId);

        }else {
            return new ConsultationDTO(consultationNew);
        }

        //return consultation != null ? new ConsultationDTO(consultation) : null;


    }
    @Transactional
    public ConsultationDTO getFirstConsultationByVisitIdReturnConsultation(Long visitId) {
        Consultation consultation = Consultation.find(
                "visit.id = ?1 ORDER BY id DESC", visitId
        ).firstResult();



        return  new ConsultationDTO(consultation);
    }

    public List<ConsultationDTO> getAllConsultations() {
        return consultationRepository.listAll(Sort.descending("id"))
                .stream()
                .map(ConsultationDTO::new)
                .toList();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public List<ConsultationDocumentDTO> listConsultationDocumentsByVisitId(Long visitId) {
        return consultationDocumentRepository
                .list("visitId = ?1", Sort.by("uploadedAt").descending(), visitId)
                .stream()
                .map(ConsultationDocumentDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response createConsultationDocumentForVisit(Long visitId, ConsultationDocumentRequest request) {
        if (visitId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit id is required", null))
                    .build();
        }
        PatientVisit visit = PatientVisit.findById(visitId);
        if (visit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Visit not found", null))
                    .build();
        }
        if (request == null || !hasText(request.fileName) || !hasText(request.fileUrl)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("File name and file URL are required", null))
                    .build();
        }

        ConsultationDocument doc = new ConsultationDocument();
        doc.visitId = visitId;
        doc.fileName = request.fileName.trim();
        doc.fileUrl = request.fileUrl.trim();
        doc.contentType = request.contentType != null ? request.contentType.trim() : null;
        doc.fileSizeBytes = request.fileSizeBytes;
        doc.description = request.description != null ? request.description.trim() : null;
        doc.uploadedBy = request.uploadedBy != null ? request.uploadedBy.trim() : null;
        doc.uploadedAt = LocalDateTime.now();
        consultationDocumentRepository.persist(doc);

        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, ConsultationDocumentDTO.from(doc))).build();
    }

    @Transactional
    public Response deleteConsultationDocumentById(Long id) {
        ConsultationDocument doc = consultationDocumentRepository.findById(id);
        if (doc == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Document not found", null))
                    .build();
        }
        consultationDocumentRepository.delete(doc);
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label, null)).build();
    }












}





