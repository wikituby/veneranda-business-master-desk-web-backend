package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.ActiveIngredient;
import org.example.inventory.item.domain.Strength;
import org.example.inventory.item.domain.StrengthUnit;
import org.example.inventory.item.domain.repositories.ActiveIngredientRepository;
import org.example.inventory.item.domain.repositories.StrengthRepository;
import org.example.inventory.item.domain.repositories.StrengthUnitRepository;
import org.example.inventory.item.services.payloads.requests.StrengthRequest;
import org.example.inventory.item.services.payloads.responses.dtos.StrengthDTO;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class StrengthService {

    @Inject
    StrengthRepository strengthRepository;

    @Inject
    ActiveIngredientRepository activeIngredientRepository;

    @Inject
    StrengthUnitRepository strengthUnitRepository;



    // ✅ CREATE
    @Transactional
    public Response addNewStrength(StrengthRequest request) {
        Long activeIngredientPk = request.activeIngredientId != null ? request.activeIngredientId : request.itemId;
        if (activeIngredientPk == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("activeIngredientId or itemId is required", null))
                    .build();
        }
        ActiveIngredient activeIngredient = activeIngredientRepository.findById(activeIngredientPk);
        if (activeIngredient == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("active ingredient not found for ID: " + activeIngredientPk))
                    .build();
        }

        StrengthUnit strengthUnit = strengthUnitRepository.findById(request.strengthUnitId);
        if (strengthUnit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("unit not found for ID: " + request.strengthUnitId))
                    .build();
        }

        Strength strength = new Strength();
        strength.activeIngredientId = activeIngredient.id;
        strength.strengthValue = request.strengthValue;
        strength.strengthUnitId = strengthUnit.id;

        strengthRepository.persist(strength);
        return Response.ok(new ResponseMessage("Strength created successfully", new StrengthDTO(strength))).build();
    }

    // ✅ UPDATE
    @Transactional
    public Response updateStrength(Long id, StrengthRequest request) {
        Strength strength = strengthRepository.findById(id);
        if (strength == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Strength not found for ID: " + id))
                    .build();
        }

        Long activeIngredientPk = request.activeIngredientId != null ? request.activeIngredientId : request.itemId;
        if (activeIngredientPk != null) {
            ActiveIngredient activeIngredient = activeIngredientRepository.findById(activeIngredientPk);
            if (activeIngredient == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("active ingredient not found for ID: " + activeIngredientPk))
                        .build();
            }
            strength.activeIngredientId = activeIngredientPk;
        }
        strength.strengthValue = request.strengthValue;
        strength.strengthUnitId = request.strengthUnitId;

        strengthRepository.persist(strength);
        return Response.ok(new ResponseMessage("Strength updated successfully", new StrengthDTO(strength))).build();
    }

    // ✅ DELETE
    @Transactional
    public Response deleteStrength(Long id) {
        Strength strength = strengthRepository.findById(id);
        if (strength == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Strength not found for ID: " + id))
                    .build();
        }

        strengthRepository.delete(strength);
        return Response.ok(new ResponseMessage("Strength deleted successfully")).build();
    }

    // ✅ GET ALL
    @Transactional
    public List<StrengthDTO> getAllStrengths() {
        return strengthRepository.listAll()
                .stream()
                .map(StrengthDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ GET BY ID
    @Transactional
    public Response getStrengthById(Long id) {
        Strength strength = strengthRepository.findById(id);
        if (strength == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Strength not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage("Strength retrieved successfully", new StrengthDTO(strength))).build();
    }
}
