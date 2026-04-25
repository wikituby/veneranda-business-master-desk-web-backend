package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.DosageForm;
import org.example.inventory.item.domain.Formulation;
import org.example.inventory.item.domain.repositories.DosageFormRepository;
import org.example.inventory.item.domain.repositories.FormulationRepository;
import org.example.inventory.item.services.payloads.requests.DosageFormRequest;
import org.example.inventory.item.services.payloads.responses.dtos.DosageFormDTO;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class DosageFormService {

    @Inject
    DosageFormRepository dosageFormRepository;

    @Inject
    FormulationRepository formulationRepository;

    /**
     * Create a new dosage form
     */
    @Transactional
    public Response createDosageForm(DosageFormRequest request) {
        // Check if a dosage form with the same name exists
        DosageForm existing = dosageFormRepository.find("name", request.name).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Dosage form '" + request.name + "' already exists."))
                    .build();
        }

        // Find the formulation associated with this dosage form
        Formulation formulation = formulationRepository.findById(request.formulationId);
        if (formulation == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Formulation not found for ID: " + request.formulationId))
                    .build();
        }

        // Create and persist the new dosage form
        DosageForm dosageForm = new DosageForm();
        dosageForm.name = request.name;
        dosageForm.formulationId = formulation.id;
        dosageForm.description = request.description;

        dosageFormRepository.persist(dosageForm);

        return Response.ok(new ResponseMessage("New dosage form added successfully", new DosageFormDTO(dosageForm))).build();
    }

    /**
     * Retrieve all dosage forms
     */
    public List<DosageFormDTO> getAllDosageForms() {
        return dosageFormRepository.listAll().stream()
                .map(DosageFormDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Find a dosage form by ID
     */
    public DosageFormDTO getDosageFormById(Long id) {
        DosageForm dosageForm = dosageFormRepository.findById(id);
        return dosageForm != null ? new DosageFormDTO(dosageForm) : null;
    }
}
