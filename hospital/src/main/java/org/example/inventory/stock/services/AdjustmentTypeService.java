package org.example.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.stock.domains.AdjustmentType;
import org.example.inventory.stock.domains.StockAdjustment;
import org.example.inventory.stock.domains.repositories.AdjustmentTypeRepository;
import org.example.inventory.stock.services.payloads.requests.AdjustmentTypeRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.AdjustmentTypeDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AdjustmentTypeService {

    @Inject
    AdjustmentTypeRepository adjustmentTypeRepository;

    @Transactional
    public Response addNew(AdjustmentTypeRequest request) {
        if (request == null || request.code == null || request.code.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("code is required", null))
                    .build();
        }
        if (request.name == null || request.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("name is required", null))
                    .build();
        }
        String code = request.code.trim().toUpperCase();
        if (adjustmentTypeRepository.find("code", code).firstResult() != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Adjustment type with code '" + code + "' already exists.", null))
                    .build();
        }

        AdjustmentType t = new AdjustmentType();
        t.code = code;
        t.name = request.name.trim();
        t.active = request.active != null ? request.active : Boolean.TRUE;
        adjustmentTypeRepository.persist(t);

        return Response.ok(new ResponseMessage("Adjustment type created successfully", new AdjustmentTypeDTO(t))).build();
    }

    @Transactional
    public Response update(Long id, AdjustmentTypeRequest request) {
        AdjustmentType t = adjustmentTypeRepository.findById(id);
        if (t == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Adjustment type not found for ID: " + id, null))
                    .build();
        }
        if (request.code != null && !request.code.isBlank()) {
            String code = request.code.trim().toUpperCase();
            AdjustmentType other = adjustmentTypeRepository.find("code", code).firstResult();
            if (other != null && !other.id.equals(id)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Another adjustment type already uses code '" + code + "'", null))
                        .build();
            }
            t.code = code;
        }
        if (request.name != null && !request.name.isBlank()) {
            t.name = request.name.trim();
        }
        if (request.active != null) {
            t.active = request.active;
        }

        return Response.ok(new ResponseMessage("Adjustment type updated successfully", new AdjustmentTypeDTO(t))).build();
    }

    @Transactional
    public Response delete(Long id) {
        AdjustmentType t = adjustmentTypeRepository.findById(id);
        if (t == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Adjustment type not found for ID: " + id, null))
                    .build();
        }
        if (StockAdjustment.count("adjustmentType.id = ?1", id) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "Cannot delete: this type is used by one or more stock adjustments. Deactivate it instead.",
                            null))
                    .build();
        }
        adjustmentTypeRepository.delete(t);
        return Response.ok(new ResponseMessage("Adjustment type deleted successfully", null)).build();
    }

    public List<AdjustmentTypeDTO> getAll() {
        return adjustmentTypeRepository.listAll().stream()
                .sorted(Comparator.comparing((AdjustmentType a) -> a.id).reversed())
                .map(AdjustmentTypeDTO::new)
                .collect(Collectors.toList());
    }

    public List<AdjustmentTypeDTO> getAllActive() {
        return adjustmentTypeRepository.list("active = ?1", Sort.descending("id"), Boolean.TRUE).stream()
                .map(AdjustmentTypeDTO::new)
                .collect(Collectors.toList());
    }

    public Response getById(Long id) {
        AdjustmentType t = adjustmentTypeRepository.findById(id);
        if (t == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Adjustment type not found for ID: " + id, null))
                    .build();
        }
        return Response.ok(new ResponseMessage("Adjustment type retrieved successfully", new AdjustmentTypeDTO(t))).build();
    }
}
