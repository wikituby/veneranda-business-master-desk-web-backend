package org.example.consultations.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.ComplaintType;
import org.example.consultations.domains.ComplaintTypeRepository;
import org.example.consultations.services.payloads.requests.ComplaintTypeRequest;
import org.example.consultations.services.payloads.responses.ComplaintTypeDTO;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ComplaintTypeService {

    @Inject
    ComplaintTypeRepository complaintTypeRepository;

    @Transactional
    public Response createComplaintType(ComplaintTypeRequest request) {
        if (request == null || request.title == null || request.title.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Complaint type title is required", null))
                    .build();
        }

        String trimmedTitle = request.title.trim();

        // Check if complaint type with same title already exists
        ComplaintType existing = complaintTypeRepository.find("title", trimmedTitle).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Complaint type with title already exists: " + trimmedTitle, null))
                    .build();
        }

        ComplaintType complaintType = new ComplaintType();
        complaintType.title = trimmedTitle;
        complaintType.description = request.description;
        complaintType.creationDate = LocalDate.now();
        complaintType.updateDate = LocalDate.now();

        complaintTypeRepository.persist(complaintType);

        return Response.ok(new ResponseMessage("Complaint type created successfully", new ComplaintTypeDTO(complaintType))).build();
    }

    @Transactional
    public Response updateComplaintType(Long id, ComplaintTypeRequest request) {
        ComplaintType complaintType = complaintTypeRepository.findById(id);
        if (complaintType == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Complaint type not found for ID: " + id, null))
                    .build();
        }

        if (request == null || request.title == null || request.title.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Complaint type title is required", null))
                    .build();
        }

        String trimmedTitle = request.title.trim();

        // Check if another complaint type with same title exists (excluding current one)
        ComplaintType existing = complaintTypeRepository.find("title = ?1 and id != ?2", trimmedTitle, id).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Another complaint type with this title already exists: " + trimmedTitle, null))
                    .build();
        }

        complaintType.title = trimmedTitle;
        complaintType.description = request.description;
        complaintType.updateDate = LocalDate.now();

        complaintTypeRepository.persist(complaintType);

        return Response.ok(new ResponseMessage("Complaint type updated successfully", new ComplaintTypeDTO(complaintType))).build();
    }

    @Transactional
    public Response deleteComplaintType(Long id) {
        ComplaintType complaintType = complaintTypeRepository.findById(id);
        if (complaintType == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Complaint type not found for ID: " + id, null))
                    .build();
        }

        // Check if any complaints are using this type
        long complaintCount = org.example.consultations.domains.Complaint.count("type.id", id);
        if (complaintCount > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Cannot delete complaint type because it is linked to existing complaints", null))
                    .build();
        }

        complaintTypeRepository.delete(complaintType);

        return Response.ok(new ResponseMessage("Complaint type deleted successfully", null)).build();
    }

    @Transactional
    public List<ComplaintTypeDTO> getAllComplaintTypes() {
        return complaintTypeRepository.listAll(Sort.descending("id"))
                .stream()
                .map(ComplaintTypeDTO::new)
                .toList();
    }

    @Transactional
    public Response getComplaintTypeById(Long id) {
        ComplaintType complaintType = complaintTypeRepository.findById(id);
        if (complaintType == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Complaint type not found for ID: " + id, null))
                    .build();
        }

        return Response.ok(new ResponseMessage("Complaint type fetched successfully", new ComplaintTypeDTO(complaintType))).build();
    }
}






















