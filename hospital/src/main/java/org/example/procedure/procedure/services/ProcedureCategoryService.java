package org.example.procedure.procedure.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

import java.time.LocalDate;
import java.util.List;


import org.example.procedure.procedure.domains.repositories.ProcedureCategoryRepository;
import org.example.procedure.procedure.services.payloads.responses.dtos.ProcedureCategoryDTO;
import org.example.procedure.procedure.domains.ProcedureCategory;
import org.example.procedure.procedure.services.payloads.requests.*;



@ApplicationScoped
public class ProcedureCategoryService {

    @Inject
    ProcedureCategoryRepository procedureCategoryRepository;

    public static final String NOT_FOUND = "Lab test not found!";
    public static final String INVALID_REQUEST = "Invalid request data!";


    @Transactional
    public Response createCategory(ProcedureCategoryRequest request) {
        // Basic validation to avoid null/blank names that can break persistence
        if (request == null || request.name == null || request.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Category name is required", null))
                    .build();
        }

        String trimmedName = request.name.trim();

        // Check if category with same name already exists
        ProcedureCategory existing = procedureCategoryRepository.find("name", trimmedName).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Category with name already exists.: " + trimmedName, null))
                    .build();
            //throw new IllegalStateException("Category with name '" + request.name + "' already exists.");
        }

        ProcedureCategory category = new ProcedureCategory();
        category.name = trimmedName;
        category.creationDate = LocalDate.now();

        // Assign parent if provided
        if (request.parentId != null) {
            ProcedureCategory parent = procedureCategoryRepository.findById(request.parentId);
            if (parent == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Parent category not found for ID: " + request.parentId))
                        .build();
                //throw new IllegalArgumentException("Parent category not found for ID: " + request.parentId);
            }
            category.parent = parent;
        }

        procedureCategoryRepository.persist(category);

        return Response.ok(new ResponseMessage("New item added successfully", new ProcedureCategoryDTO(category))).build();

        //return category;
    }

    @Transactional
    public Response updateCategory(ProcedureCategoryUpdateRequest request) {
        // Find category by ID
        ProcedureCategory category = procedureCategoryRepository.findById(request.categoryId);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Category not found for ID: " + request.categoryId))
                    .build();
        }

        // Check if another category with the same name exists
        ProcedureCategory existing = procedureCategoryRepository.find("name", request.name).firstResult();
        if (existing != null && !existing.id.equals(request.categoryId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Another category with the same name already exists: " + request.name, null))
                    .build();
        }

        // Update name
        category.name = request.name;
        category.LastUpdatedDate = LocalDate.now();

        // Update parent if provided
        if (request.parentId != null) {
            ProcedureCategory parent = procedureCategoryRepository.findById(request.parentId);
            if (parent == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Parent category not found for ID: " + request.parentId))
                        .build();
            }
            // Prevent circular reference
            if (parent.id.equals(request.categoryId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("A category cannot be its own parent.", null))
                        .build();
            }
            category.parent = parent;
        } else {
            category.parent = null; // Remove parent if not specified
        }

        // Persist changes
        procedureCategoryRepository.persist(category);

        return Response.ok(new ResponseMessage("Category updated successfully", new ProcedureCategoryDTO(category))).build();
    }


    @Transactional
    public Response deleteCategory(Long id) {
        ProcedureCategory category = procedureCategoryRepository.findById(id);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Category not found for ID: " + id))
                    .build();
        }

        // Optional: Check if it has subcategories
        long subCount = procedureCategoryRepository.count("parent.id", id);
        if (subCount > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Cannot delete category because it has subcategories. first delete all subCategories", null))
                    .build();
        }

        // Optional: Check if any items belong to this category
        // long itemCount = itemRepository.count("category.id", id);
        // if (itemCount > 0) {
        //     return Response.status(Response.Status.BAD_REQUEST)
        //             .entity(new ResponseMessage("Cannot delete category because it is linked to existing items.", null))
        //             .build();
        // }

        procedureCategoryRepository.delete(category);

        return Response.ok(new ResponseMessage("Category deleted successfully", null)).build();
    }

    @Transactional
    public List<ProcedureCategoryDTO> getAllItemCategories() {
        var categories = procedureCategoryRepository.listAll();

        // Convert entity list to DTO list
        return categories.stream()
                .map(ProcedureCategoryDTO::new)
                .toList();
    }

    @Transactional
    public Response getCategoryById(Long id) {
        ProcedureCategory category = procedureCategoryRepository.findById(id);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Category not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, new ProcedureCategoryDTO(category))).build();
    }







}






