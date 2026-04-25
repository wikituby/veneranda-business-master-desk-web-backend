package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Formulation;
import org.example.inventory.item.domain.repositories.FormulationRepository;
import org.example.inventory.item.services.payloads.requests.FormulationRequest;
import org.example.inventory.item.services.payloads.responses.dtos.FormulationDTO;
import org.example.inventory.item.services.payloads.responses.dtos.ItemDTO;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class FormulationService {

    @Inject
    FormulationRepository formulationRepository;

    /**
     * Create a new formulation
     */
    @Transactional
    public Response createFormulation(FormulationRequest request) {
        // Check if formulation with same name exists
        Formulation existing = formulationRepository.find("name", request.name).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Formulation '" + request.name + "' already exists."))
                    .build();
        }

        Formulation formulation = new Formulation();
        formulation.name = request.name;
        formulation.description = request.description;

        formulationRepository.persist(formulation);

        return Response.ok(new ResponseMessage("New formulation added successfully", new FormulationDTO(formulation))).build();

    }

    /**
     * List all formulations
     */
    public List<FormulationDTO> getAllFormulations() {
        List<Formulation> formulations = formulationRepository.listAll();
        List<FormulationDTO> dtos = new ArrayList<>();
        for (Formulation f : formulations) {
            dtos.add(new FormulationDTO(f));
        }
        return dtos;
    }

    /**
     * Get a formulation by ID
     */
    public FormulationDTO getFormulationById(Long id) {
        Formulation formulation = formulationRepository.findById(id);
        if (formulation == null) {
            throw new IllegalArgumentException("Formulation not found with ID: " + id);
        }
        return new FormulationDTO(formulation);
    }

    /**
     * Update a formulation
     */
    @Transactional
    public Response updateFormulation(Long id, FormulationRequest request) {
        Formulation formulation = formulationRepository.findById(id);
        if (formulation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Formulation not found for ID: " + id))
                    .build();
        }

        // Check if another formulation with the same name exists
        Formulation existing = formulationRepository.find("name", request.name).firstResult();
        if (existing != null && !existing.id.equals(id)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Formulation with name '" + request.name + "' already exists."))
                    .build();
        }

        formulation.name = request.name;
        formulation.description = request.description;

        return Response.ok(new ResponseMessage("Formulation updated successfully", new FormulationDTO(formulation))).build();
    }

    /**
     * Delete a formulation
     */
    @Transactional
    public Response deleteFormulation(Long id) {
        Formulation formulation = formulationRepository.findById(id);
        if (formulation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Formulation not found for ID: " + id))
                    .build();
        }

        formulationRepository.delete(formulation);
        return Response.ok(new ResponseMessage("Formulation deleted successfully")).build();
    }
}
