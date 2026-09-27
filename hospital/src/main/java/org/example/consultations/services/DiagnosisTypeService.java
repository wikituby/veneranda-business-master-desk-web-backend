package org.example.consultations.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.DiagnosisType;
import org.example.consultations.domains.DiagnosisTypeRepository;
import org.example.consultations.services.payloads.requests.DiagnosisTypeRequest;
import org.example.consultations.services.payloads.responses.DiagnosisTypeDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class DiagnosisTypeService {

    @Inject
    DiagnosisTypeRepository diagnosisTypeRepository;

    @Transactional
    public Response create(DiagnosisTypeRequest request) {
        if (request == null || request.title == null || request.title.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Diagnosis type title is required", null))
                    .build();
        }

        String trimmedTitle = request.title.trim();
        DiagnosisType existing = diagnosisTypeRepository
                .find("lower(title)", trimmedTitle.toLowerCase(Locale.ROOT))
                .firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Diagnosis type already exists: " + trimmedTitle, null))
                    .build();
        }

        DiagnosisType row = new DiagnosisType();
        row.title = trimmedTitle;
        row.description = blankToNull(request.description);
        row.hmisCode = blankToNull(request.hmisCode);
        row.icd10Code = blankToNull(request.icd10Code);
        row.matchKeywords = blankToNull(request.matchKeywords);
        row.creationDate = LocalDate.now();
        row.updateDate = LocalDate.now();
        diagnosisTypeRepository.persist(row);

        return Response.ok(new ResponseMessage("Diagnosis type created successfully", new DiagnosisTypeDTO(row))).build();
    }

    @Transactional
    public Response update(Long id, DiagnosisTypeRequest request) {
        DiagnosisType row = diagnosisTypeRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Diagnosis type not found for ID: " + id, null))
                    .build();
        }
        if (request == null || request.title == null || request.title.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Diagnosis type title is required", null))
                    .build();
        }

        String trimmedTitle = request.title.trim();
        DiagnosisType existing = diagnosisTypeRepository
                .find("lower(title) = ?1 and id != ?2", trimmedTitle.toLowerCase(Locale.ROOT), id)
                .firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Another diagnosis type with this title already exists", null))
                    .build();
        }

        row.title = trimmedTitle;
        row.description = blankToNull(request.description);
        row.hmisCode = blankToNull(request.hmisCode);
        row.icd10Code = blankToNull(request.icd10Code);
        row.matchKeywords = blankToNull(request.matchKeywords);
        row.updateDate = LocalDate.now();
        diagnosisTypeRepository.persist(row);

        return Response.ok(new ResponseMessage("Diagnosis type updated successfully", new DiagnosisTypeDTO(row))).build();
    }

    @Transactional
    public Response delete(Long id) {
        DiagnosisType row = diagnosisTypeRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Diagnosis type not found for ID: " + id, null))
                    .build();
        }
        diagnosisTypeRepository.delete(row);
        return Response.ok(new ResponseMessage("Diagnosis type deleted successfully", null)).build();
    }

    @Transactional
    public List<DiagnosisTypeDTO> listAll() {
        return diagnosisTypeRepository.listAll(Sort.ascending("title")).stream()
                .map(DiagnosisTypeDTO::new)
                .toList();
    }

    @Transactional
    public Response getById(Long id) {
        DiagnosisType row = diagnosisTypeRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Diagnosis type not found for ID: " + id, null))
                    .build();
        }
        return Response.ok(new ResponseMessage("Diagnosis type fetched successfully", new DiagnosisTypeDTO(row))).build();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
