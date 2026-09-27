package org.example.consultations.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.ComplaintOption;
import org.example.consultations.domains.ComplaintOptionRepository;
import org.example.consultations.services.payloads.requests.ComplaintOptionRequest;
import org.example.consultations.services.payloads.responses.ComplaintOptionDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@ApplicationScoped
public class ComplaintOptionService {

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "nature", "aggravating", "alleviating", "associated"
    );

    @Inject
    ComplaintOptionRepository complaintOptionRepository;

    @Transactional
    public Response create(ComplaintOptionRequest request) {
        String category = normalizeCategory(request != null ? request.category : null);
        if (category == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Valid category is required (nature, aggravating, alleviating, associated)", null))
                    .build();
        }
        if (request == null || request.title == null || request.title.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Complaint option title is required", null))
                    .build();
        }

        String trimmedTitle = request.title.trim();
        ComplaintOption existing = complaintOptionRepository
                .find("category = ?1 and lower(title) = ?2", category, trimmedTitle.toLowerCase(Locale.ROOT))
                .firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Option already exists for this category: " + trimmedTitle, null))
                    .build();
        }

        ComplaintOption row = new ComplaintOption();
        row.category = category;
        row.title = trimmedTitle;
        row.description = blankToNull(request.description);
        row.creationDate = LocalDate.now();
        row.updateDate = LocalDate.now();
        complaintOptionRepository.persist(row);

        return Response.ok(new ResponseMessage("Complaint option created successfully", new ComplaintOptionDTO(row))).build();
    }

    @Transactional
    public Response update(Long id, ComplaintOptionRequest request) {
        ComplaintOption row = complaintOptionRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Complaint option not found for ID: " + id, null))
                    .build();
        }

        String category = request != null && request.category != null && !request.category.isBlank()
                ? normalizeCategory(request.category)
                : row.category;
        if (category == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Valid category is required (nature, aggravating, alleviating, associated)", null))
                    .build();
        }
        if (request == null || request.title == null || request.title.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Complaint option title is required", null))
                    .build();
        }

        String trimmedTitle = request.title.trim();
        ComplaintOption existing = complaintOptionRepository
                .find("category = ?1 and lower(title) = ?2 and id != ?3", category, trimmedTitle.toLowerCase(Locale.ROOT), id)
                .firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Another option with this title already exists in the category", null))
                    .build();
        }

        row.category = category;
        row.title = trimmedTitle;
        row.description = blankToNull(request.description);
        row.updateDate = LocalDate.now();
        complaintOptionRepository.persist(row);

        return Response.ok(new ResponseMessage("Complaint option updated successfully", new ComplaintOptionDTO(row))).build();
    }

    @Transactional
    public Response delete(Long id) {
        ComplaintOption row = complaintOptionRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Complaint option not found for ID: " + id, null))
                    .build();
        }
        complaintOptionRepository.delete(row);
        return Response.ok(new ResponseMessage("Complaint option deleted successfully", null)).build();
    }

    @Transactional
    public List<ComplaintOptionDTO> listAll() {
        return complaintOptionRepository.listAll(Sort.descending("id")).stream()
                .map(ComplaintOptionDTO::new)
                .toList();
    }

    @Transactional
    public List<ComplaintOptionDTO> listByCategory(String categoryRaw) {
        String category = normalizeCategory(categoryRaw);
        if (category == null) {
            return List.of();
        }
        return complaintOptionRepository
                .list("category", Sort.ascending("title"), category)
                .stream()
                .map(ComplaintOptionDTO::new)
                .toList();
    }

    @Transactional
    public Response getById(Long id) {
        ComplaintOption row = complaintOptionRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Complaint option not found for ID: " + id, null))
                    .build();
        }
        return Response.ok(new ResponseMessage("Complaint option fetched successfully", new ComplaintOptionDTO(row))).build();
    }

    private String normalizeCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);
        return ALLOWED_CATEGORIES.contains(value) ? value : null;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
