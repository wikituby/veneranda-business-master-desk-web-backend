package org.example.consultations.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.ComplaintSite;
import org.example.consultations.domains.ComplaintSiteRepository;
import org.example.consultations.services.payloads.requests.ComplaintSiteRequest;
import org.example.consultations.services.payloads.responses.ComplaintSiteDTO;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ComplaintSiteService {

    @Inject
    ComplaintSiteRepository complaintSiteRepository;

    @Transactional
    public Response createComplaintSite(ComplaintSiteRequest request) {
        if (request == null || request.title == null || request.title.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Complaint site title is required", null))
                    .build();
        }

        String trimmedTitle = request.title.trim();

        // Check if complaint site with same title already exists
        ComplaintSite existing = complaintSiteRepository.find("title", trimmedTitle).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Complaint site with title already exists: " + trimmedTitle, null))
                    .build();
        }

        ComplaintSite complaintSite = new ComplaintSite();
        complaintSite.title = trimmedTitle;
        complaintSite.description = request.description;
        complaintSite.creationDate = LocalDate.now();
        complaintSite.updateDate = LocalDate.now();

        complaintSiteRepository.persist(complaintSite);

        return Response.ok(new ResponseMessage("Complaint site created successfully", new ComplaintSiteDTO(complaintSite))).build();
    }

    @Transactional
    public Response updateComplaintSite(Long id, ComplaintSiteRequest request) {
        ComplaintSite complaintSite = complaintSiteRepository.findById(id);
        if (complaintSite == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Complaint site not found for ID: " + id, null))
                    .build();
        }

        if (request == null || request.title == null || request.title.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Complaint site title is required", null))
                    .build();
        }

        String trimmedTitle = request.title.trim();

        // Check if another complaint site with same title exists (excluding current one)
        ComplaintSite existing = complaintSiteRepository.find("title = ?1 and id != ?2", trimmedTitle, id).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Another complaint site with this title already exists: " + trimmedTitle, null))
                    .build();
        }

        complaintSite.title = trimmedTitle;
        complaintSite.description = request.description;
        complaintSite.updateDate = LocalDate.now();

        complaintSiteRepository.persist(complaintSite);

        return Response.ok(new ResponseMessage("Complaint site updated successfully", new ComplaintSiteDTO(complaintSite))).build();
    }

    @Transactional
    public Response deleteComplaintSite(Long id) {
        ComplaintSite complaintSite = complaintSiteRepository.findById(id);
        if (complaintSite == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Complaint site not found for ID: " + id, null))
                    .build();
        }

        // Check if any complaints are using this site
        long complaintCount = org.example.consultations.domains.Complaint.count("site.id", id);
        if (complaintCount > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Cannot delete complaint site because it is linked to existing complaints", null))
                    .build();
        }

        complaintSiteRepository.delete(complaintSite);

        return Response.ok(new ResponseMessage("Complaint site deleted successfully", null)).build();
    }

    @Transactional
    public List<ComplaintSiteDTO> getAllComplaintSites() {
        return complaintSiteRepository.listAll(Sort.descending("id"))
                .stream()
                .map(ComplaintSiteDTO::new)
                .toList();
    }

    @Transactional
    public Response getComplaintSiteById(Long id) {
        ComplaintSite complaintSite = complaintSiteRepository.findById(id);
        if (complaintSite == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Complaint site not found for ID: " + id, null))
                    .build();
        }

        return Response.ok(new ResponseMessage("Complaint site fetched successfully", new ComplaintSiteDTO(complaintSite))).build();
    }
}






















