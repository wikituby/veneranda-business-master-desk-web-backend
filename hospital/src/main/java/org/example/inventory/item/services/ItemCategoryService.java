package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.ItemCategory;
import org.example.inventory.item.domain.repositories.ItemCategoryRepository;
import org.example.inventory.item.services.payloads.requests.ItemCategoryRequest;
import org.example.inventory.item.services.payloads.requests.ItemCategoryUpdateRequest;
import org.example.inventory.item.services.payloads.responses.dtos.ItemCategoryDTO;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ItemCategoryService {

    @Inject
    ItemCategoryRepository itemCategoryRepository;

    public static final String NOT_FOUND = "Item category not found!";
    public static final String INVALID_REQUEST = "Invalid request data!";

    @Transactional
    public Response createCategory(ItemCategoryRequest request) {
        if (request == null || request.name == null || request.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Category name is required", null))
                    .build();
        }

        String trimmedName = request.name.trim();

        ItemCategory existing = itemCategoryRepository.find("name", trimmedName).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Category with name already exists: " + trimmedName, null))
                    .build();
        }

        ItemCategory category = new ItemCategory();
        category.name = trimmedName;
        category.creationDate = LocalDate.now();

        if (request.parentId != null) {
            ItemCategory parent = itemCategoryRepository.findById(request.parentId);
            if (parent == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Parent category not found for ID: " + request.parentId))
                        .build();
            }
            category.parent = parent;
        }

        itemCategoryRepository.persist(category);

        return Response.ok(new ResponseMessage("New item category added successfully", new ItemCategoryDTO(category))).build();
    }

    @Transactional
    public Response updateCategory(ItemCategoryUpdateRequest request) {
        ItemCategory category = itemCategoryRepository.findById(request.categoryId);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Category not found for ID: " + request.categoryId))
                    .build();
        }

        ItemCategory existing = itemCategoryRepository.find("name", request.name).firstResult();
        if (existing != null && !existing.id.equals(request.categoryId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Another category with the same name already exists: " + request.name, null))
                    .build();
        }

        category.name = request.name;
        category.lastUpdatedDate = LocalDate.now();

        if (request.parentId != null) {
            ItemCategory parent = itemCategoryRepository.findById(request.parentId);
            if (parent == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Parent category not found for ID: " + request.parentId))
                        .build();
            }
            if (parent.id.equals(request.categoryId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("A category cannot be its own parent.", null))
                        .build();
            }
            category.parent = parent;
        } else {
            category.parent = null;
        }

        itemCategoryRepository.persist(category);

        return Response.ok(new ResponseMessage("Item category updated successfully", new ItemCategoryDTO(category))).build();
    }

    @Transactional
    public Response deleteCategory(Long id) {
        ItemCategory category = itemCategoryRepository.findById(id);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Category not found for ID: " + id))
                    .build();
        }

        long subCount = itemCategoryRepository.count("parent.id", id);
        if (subCount > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Cannot delete category because it has subcategories. First delete all subcategories.", null))
                    .build();
        }

        long itemsAsCategory = Item.count("category.id", id);
        long itemsAsParentCategory = Item.count("parentCategory.id", id);
        if (itemsAsCategory > 0 || itemsAsParentCategory > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Cannot delete category because it is linked to existing items. Unlink or reassign items first.", null))
                    .build();
        }

        itemCategoryRepository.delete(category);

        return Response.ok(new ResponseMessage("Item category deleted successfully", null)).build();
    }

    @Transactional
    public List<ItemCategoryDTO> getAllItemCategories() {
        var categories = itemCategoryRepository.listAll();
        return categories.stream()
                .map(ItemCategoryDTO::new)
                .toList();
    }

    @Transactional
    public Response getCategoryById(Long id) {
        ItemCategory category = itemCategoryRepository.findById(id);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Category not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, new ItemCategoryDTO(category))).build();
    }
}





