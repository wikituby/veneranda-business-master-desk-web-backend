package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Composition;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.ProductVariant;
import org.example.inventory.item.domain.repositories.CompositionRepository;
import org.example.inventory.item.domain.repositories.ItemRepository;
import org.example.inventory.item.domain.repositories.ProductVariantRepository;
import org.example.inventory.item.services.payloads.requests.CompositionRequest;
import org.example.inventory.item.services.payloads.responses.dtos.CompositionDTO;
import org.example.inventory.item.services.payloads.responses.dtos.ItemDTO;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CompositionService {

    @Inject
    CompositionRepository compositionRepository;

    @Inject
    ProductVariantRepository variantRepository;

    @Inject
    ItemRepository itemRepository;


    @Transactional
    public Response createComposition(CompositionRequest request) {

        Composition composition = new Composition();
        composition.stockItemId = request.stockItemId;
        composition.strengthId = request.strengthId;

        compositionRepository.persist(composition);

        return Response.ok(new ResponseMessage("New composition created successfully", new CompositionDTO(composition))).build();

    }

    /**
     * Get all compositions
     */
    @Transactional
    public List<CompositionDTO> getAllCompositions() {
        return compositionRepository.listAll()
                .stream()
                .map(CompositionDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get compositions for a specific ProductVariant
     */
    public List<Composition> getCompositionsByVariantId(Long stockItemId) {
        return compositionRepository.find("stockItemId", stockItemId).list();
    }

    /**
     * Get composition by ID
     */
    @Transactional
    public Response getCompositionById(Long id) {
        Composition composition = compositionRepository.findById(id);
        if (composition == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Composition not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, new CompositionDTO(composition))).build();
    }

    /**
     * Update composition
     */
    @Transactional
    public Response updateComposition(Long id, CompositionRequest request) {
        Composition composition = compositionRepository.findById(id);
        if (composition == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Composition not found for ID: " + id))
                    .build();
        }

        composition.stockItemId = request.stockItemId;
        composition.strengthId = request.strengthId;

        return Response.ok(new ResponseMessage("Composition updated successfully", new CompositionDTO(composition))).build();
    }

    /**
     * Delete a composition by ID
     */
    @Transactional
    public Response deleteComposition(Long compositionId) {
        Composition composition = compositionRepository.findById(compositionId);
        if (composition == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Composition not found for ID: " + compositionId))
                    .build();
        }

        compositionRepository.delete(composition);
        return Response.ok(new ResponseMessage("Composition deleted successfully")).build();
    }
}
