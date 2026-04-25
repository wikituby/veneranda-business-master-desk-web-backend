package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Category;
import org.example.inventory.item.domain.repositories.CategoryRepository;

import jakarta.inject.Inject;
import org.example.configuration.handler.ActionMessages;
import org.example.inventory.item.services.payloads.requests.CategoryRequest;
import org.example.inventory.item.services.payloads.requests.ItemCategoryUpdateRequest;
import org.example.inventory.item.services.payloads.responses.dtos.CategoryDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class CategoryService {

    @Inject
    CategoryRepository categoryRepository;


    @Transactional
    public Response createCategory(CategoryRequest request) {
        // Check if category with same name already exists
        Category existing = categoryRepository.find("name", request.name).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Category with name already exists.: " + request.name, null))
                    .build();
            //throw new IllegalStateException("Category with name '" + request.name + "' already exists.");
        }

        Category category = new Category();
        category.name = request.name;

        // Assign parent if provided
        if (request.parentId != null) {
            Category parent = categoryRepository.findById(request.parentId);
            if (parent == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Parent category not found for ID: " + request.parentId))
                        .build();
                //throw new IllegalArgumentException("Parent category not found for ID: " + request.parentId);
            }
            category.parent = parent;
        }

        categoryRepository.persist(category);

        return Response.ok(new ResponseMessage("New item added successfully", new CategoryDTO(category))).build();

        //return category;
    }

    @Transactional
    public Response updateCategory(ItemCategoryUpdateRequest request) {
        // Find category by ID
        Category category = categoryRepository.findById(request.categoryId);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Category not found for ID: " + request.categoryId))
                    .build();
        }

        // Check if another category with the same name exists
        Category existing = categoryRepository.find("name", request.name).firstResult();
        if (existing != null && !existing.id.equals(request.categoryId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Another category with the same name already exists: " + request.name, null))
                    .build();
        }

        // Update name
        category.name = request.name;

        // Update parent if provided
        if (request.parentId != null) {
            Category parent = categoryRepository.findById(request.parentId);
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
        categoryRepository.persist(category);

        return Response.ok(new ResponseMessage("Category updated successfully", new CategoryDTO(category))).build();
    }
    @Transactional
    public Response deleteCategory(Long id) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Category not found for ID: " + id))
                    .build();
        }

        // Optional: Check if it has subcategories
        long subCount = categoryRepository.count("parent.id", id);
        if (subCount > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Cannot delete category because it has subcategories.", null))
                    .build();
        }

        // Optional: Check if any items belong to this category
        // long itemCount = itemRepository.count("category.id", id);
        // if (itemCount > 0) {
        //     return Response.status(Response.Status.BAD_REQUEST)
        //             .entity(new ResponseMessage("Cannot delete category because it is linked to existing items.", null))
        //             .build();
        // }

        categoryRepository.delete(category);

        return Response.ok(new ResponseMessage("Category deleted successfully", null)).build();
    }

    @Transactional
    public List<CategoryDTO> getAllItemCategories() {
        var categories = categoryRepository.listAll();

        // Convert entity list to DTO list
        return categories.stream()
                .map(CategoryDTO::new)
                .toList();
    }

    @Transactional
    public Response getCategoryById(Long id) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Category not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, new CategoryDTO(category))).build();
    }

    @Transactional
    public CategoryDTO getHighestParentCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId);
        if (category == null) {
            return null;
        }

        Set<Long> visited = new HashSet<>();
        Category current = category;

        while (current.parent != null) {
            if (!visited.add(current.id)) {
                throw new IllegalStateException("Circular category hierarchy detected for category ID: " + categoryId);
            }
            current = current.parent;
        }

        return new CategoryDTO(current);
    }










}
