package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.StrengthUnit;
import org.example.inventory.item.domain.repositories.StrengthUnitRepository;
import org.example.inventory.item.services.payloads.requests.StrengthUnitRequest;
import org.example.inventory.item.services.payloads.responses.dtos.StrengthUnitDTO;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class StrengthUnitService {

    @Inject
    StrengthUnitRepository strengthUnitRepository;

    // ✅ CREATE
    @Transactional
    public Response addNewStrengthUnit(StrengthUnitRequest request) {
        StrengthUnit existing = strengthUnitRepository.find("title", request.title).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Strength unit with title '" + request.title + "' already exists."))
                    .build();
        }

        StrengthUnit unit = new StrengthUnit();
        unit.title = request.title;
        unit.standardAbbreviation = request.standardAbbreviation;
        unit.description = request.description;

        strengthUnitRepository.persist(unit);

        return Response.ok(new ResponseMessage("Strength unit created successfully", new StrengthUnitDTO(unit))).build();
    }

    // ✅ UPDATE
    @Transactional
    public Response updateStrengthUnit(Long id, StrengthUnitRequest request) {
        StrengthUnit unit = strengthUnitRepository.findById(id);
        if (unit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Strength unit not found for ID: " + id))
                    .build();
        }

        unit.title = request.title;
        unit.standardAbbreviation = request.standardAbbreviation;
        unit.description = request.description;

        return Response.ok(new ResponseMessage("Strength unit updated successfully", new StrengthUnitDTO(unit))).build();
    }

    // ✅ DELETE
    @Transactional
    public Response deleteStrengthUnit(Long id) {
        StrengthUnit unit = strengthUnitRepository.findById(id);
        if (unit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Strength unit not found for ID: " + id))
                    .build();
        }

        strengthUnitRepository.delete(unit);
        return Response.ok(new ResponseMessage("Strength unit deleted successfully")).build();
    }

    // ✅ GET ALL
    @Transactional
    public List<StrengthUnitDTO> getAllStrengthUnits() {
        return strengthUnitRepository.listAll()
                .stream()
                .map(StrengthUnitDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ GET BY ID
    @Transactional
    public Response getStrengthUnitById(Long id) {
        StrengthUnit unit = strengthUnitRepository.findById(id);
        if (unit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Strength unit not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage("Strength unit retrieved successfully", new StrengthUnitDTO(unit))).build();
    }
}
