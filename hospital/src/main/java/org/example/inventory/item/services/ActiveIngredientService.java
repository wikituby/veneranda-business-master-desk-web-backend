package org.example.inventory.item.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.auth.services.UserAuthService;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.ActiveIngredient;
import org.example.inventory.item.domain.Category;
import org.example.inventory.item.domain.repositories.ActiveIngredientRepository;
import org.example.inventory.item.domain.repositories.CategoryRepository;
import org.example.inventory.item.services.payloads.requests.ActiveIngredientRequest;
import org.example.inventory.item.services.payloads.requests.ActiveIngredientUpdateRequest;
import org.example.inventory.item.services.payloads.responses.dtos.ActiveIngredientDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ActiveIngredientService {

    @Inject
    ActiveIngredientRepository activeIngredientRepository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    UserAuthService userAuthService;

    private static final String NOT_FOUND = "Not found!";

    @Transactional
    public Response addActiveIngredient(ActiveIngredientRequest request) {
        if (request.genericName == null || request.genericName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("genericName is required", null))
                    .build();
        }
        if (request.categoryId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("categoryId is required", null))
                    .build();
        }

        ActiveIngredient existing = activeIngredientRepository.find(
                "LOWER(genericName) = ?1",
                request.genericName.toLowerCase()
        ).firstResult();

        if (existing != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage(
                            "An active ingredient with the name '" + request.genericName + "' already exists.",
                            null))
                    .build();
        }

        Category category = categoryRepository.findById(request.categoryId);

        if (category == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "Item Category not found for ID: " + request.categoryId, null))
                    .build();
        }

        ActiveIngredient activeIngredient = new ActiveIngredient();
        activeIngredient.number = userAuthService.generateRandomPassword(5);
        activeIngredient.genericName = request.genericName;

        activeIngredient.categoryName = category.name;
        activeIngredient.categoryId = category.id;

        activeIngredient.indication = request.indication;
        activeIngredient.contraIndication = request.contraIndication;
        activeIngredient.description = request.description;

        activeIngredient.creationDate = LocalDate.now();
        activeIngredient.updateDate = LocalDate.now();

        activeIngredientRepository.persist(activeIngredient);

        return Response.ok(
                new ResponseMessage("New active ingredient added successfully", new ActiveIngredientDTO(activeIngredient))
        ).build();
    }

    @Transactional
    public List<ActiveIngredient> addActiveIngredients(List<ActiveIngredientRequest> requests) {
        List<ActiveIngredient> created = new ArrayList<>();

        for (ActiveIngredientRequest request : requests) {
            Category category = categoryRepository.findById(request.categoryId);
            if (category == null) {
                throw new IllegalArgumentException("Item category not found for ID: " + request.categoryId);
            }

            ActiveIngredient activeIngredient = new ActiveIngredient();
            activeIngredient.number = userAuthService.generateRandomPassword(5);
            activeIngredient.genericName = request.genericName;
            activeIngredient.categoryId = category.id;
            activeIngredient.categoryName = category.name;
            activeIngredient.indication = request.indication;
            activeIngredient.contraIndication = request.contraIndication;
            activeIngredient.description = request.description;
            activeIngredient.creationDate = LocalDate.now();

            activeIngredientRepository.persist(activeIngredient);
            created.add(activeIngredient);
        }

        return created;
    }

    public List<ActiveIngredient> getAllActiveIngredientsRaw() {
        return activeIngredientRepository.listAll();
    }

    @Transactional
    public List<ActiveIngredientDTO> listLatestFirst() {
        return activeIngredientRepository
                .listAll(Sort.descending("id"))
                .stream()
                .map(ActiveIngredientDTO::new)
                .collect(Collectors.toList());
    }

    public ActiveIngredient getActiveIngredientById(Long id) {
        return activeIngredientRepository.findById(id);
    }

    public void deleteAllActiveIngredients() {
        activeIngredientRepository.deleteAll();
    }

    @Transactional
    public Response deleteActiveIngredientById(Long id) {
        ActiveIngredient row = activeIngredientRepository.findById(id);
        if (row == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("active ingredient not found", null))
                    .build();
        }
        activeIngredientRepository.delete(row);
        return Response.ok(new ResponseMessage("Active ingredient deleted successfully")).build();
    }

    public ActiveIngredientDTO updateActiveIngredientById(Long id, ActiveIngredientUpdateRequest request) {
        if (request.categoryId == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("categoryId is required", null))
                    .build());
        }
        Category category = categoryRepository.findById(request.categoryId);
        if (category == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Item Category not found for ID: " + request.categoryId, null))
                    .build());
        }

        return activeIngredientRepository.findByIdOptional(id)
                .map(activeIngredient -> {

                    activeIngredient.genericName = request.genericName;
                    activeIngredient.description = request.description;

                    activeIngredient.categoryId = category.id;
                    activeIngredient.categoryName = category.name;
                    activeIngredient.updateDate = LocalDate.now();

                    activeIngredientRepository.persist(activeIngredient);

                    return new ActiveIngredientDTO(activeIngredient);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND, 404));
    }

}
