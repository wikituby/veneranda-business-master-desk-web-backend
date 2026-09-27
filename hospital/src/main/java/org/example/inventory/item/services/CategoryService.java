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
import org.example.inventory.item.domain.ActiveIngredient;
import org.example.inventory.stock.domains.StockItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class CategoryService {

    @Inject
    CategoryRepository categoryRepository;


    @Transactional
    public Response createCategory(CategoryRequest request) {
        if (request == null || request.name == null || request.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Category name is required.", null))
                    .build();
        }

        String trimmedName = request.name.trim();

        // Names must be unique per parent (siblings), not globally across the whole tree.
        Category existing;
        if (request.parentId != null) {
            existing = categoryRepository
                    .find("name = ?1 and parent.id = ?2", trimmedName, request.parentId)
                    .firstResult();
        } else {
            existing = categoryRepository
                    .find("name = ?1 and parent is null", trimmedName)
                    .firstResult();
        }

        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Category with name already exists under this parent: " + trimmedName, null))
                    .build();
        }

        Category category = new Category();
        category.name = trimmedName;

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

        String trimmedName = request.name == null ? "" : request.name.trim();
        if (trimmedName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Category name is required.", null))
                    .build();
        }

        Long targetParentId = request.parentId;
        Category existing;
        if (targetParentId != null) {
            existing = categoryRepository
                    .find("name = ?1 and parent.id = ?2", trimmedName, targetParentId)
                    .firstResult();
        } else {
            existing = categoryRepository
                    .find("name = ?1 and parent is null", trimmedName)
                    .firstResult();
        }

        if (existing != null && !existing.id.equals(request.categoryId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Another category with the same name already exists under this parent: " + trimmedName, null))
                    .build();
        }

        category.name = trimmedName;

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
                    .entity(new ResponseMessage("Category not found for ID: " + id, null))
                    .build();
        }

        List<Long> categoryIdsToDelete = collectCategoryTreeIds(id);
        int reassignedItems = reassignLinkedItemsBeforeDelete(categoryIdsToDelete);

        int deletedCount = deleteCategoryTree(id);
        categoryRepository.getEntityManager().flush();

        String message = deletedCount > 1
                ? "Category and " + (deletedCount - 1) + " subcategories deleted successfully"
                : "Category deleted successfully";

        if (reassignedItems > 0) {
            message += ". " + reassignedItems + " linked item(s) were moved to the parent category"
                    + " (or left without a category when no parent exists).";
        }

        return Response.ok(new ResponseMessage(message, null)).build();
    }

    /**
     * Deletes a category and every descendant, deepest nodes first.
     * Uses bulk JPQL deletes so child rows are actually removed from the database.
     */
    private int deleteCategoryTree(Long id) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            return 0;
        }

        int deletedCount = 0;
        for (Category child : categoryRepository.list("parent.id", id)) {
            deletedCount += deleteCategoryTree(child.id);
        }

        categoryRepository.getEntityManager()
                .createQuery("DELETE FROM Category c WHERE c.id = :id")
                .setParameter("id", id)
                .executeUpdate();

        return deletedCount + 1;
    }

    /** Collect every category id in the subtree (parent + all descendants). */
    private List<Long> collectCategoryTreeIds(Long id) {
        List<Long> ids = new ArrayList<>();
        collectCategoryTreeIdsRecursive(id, ids);
        return ids;
    }

    private void collectCategoryTreeIdsRecursive(Long id, List<Long> ids) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            return;
        }
        for (Category child : categoryRepository.list("parent.id", id)) {
            collectCategoryTreeIdsRecursive(child.id, ids);
        }
        ids.add(id);
    }

    /**
     * Before deleting categories, move linked stock items and active ingredients to each
     * deleted category's parent. Root categories with no parent leave items uncategorized.
     * Processes deepest categories first so items bubble up correctly through the tree.
     */
    private int reassignLinkedItemsBeforeDelete(List<Long> categoryIdsToDelete) {
        if (categoryIdsToDelete == null || categoryIdsToDelete.isEmpty()) {
            return 0;
        }

        List<Long> orderedIds = sortCategoryIdsByDepthDescending(categoryIdsToDelete);
        int reassignedTotal = 0;

        for (Long categoryId : orderedIds) {
            Category category = categoryRepository.findById(categoryId);
            if (category == null) {
                continue;
            }

            Category parent = category.parent;
            Long newParentId = parent != null ? parent.id : null;
            String newParentName = parent != null ? parent.name : null;

            reassignedTotal += reassignStockItemsFromCategory(categoryId, newParentId, newParentName);
            reassignedTotal += reassignActiveIngredientsFromCategory(categoryId, newParentId, newParentName);
        }

        return reassignedTotal;
    }

    private List<Long> sortCategoryIdsByDepthDescending(List<Long> categoryIds) {
        List<Long> ordered = new ArrayList<>(categoryIds);
        ordered.sort((leftId, rightId) -> Integer.compare(getCategoryDepth(rightId), getCategoryDepth(leftId)));
        return ordered;
    }

    private int getCategoryDepth(Long categoryId) {
        int depth = 0;
        Category current = categoryRepository.findById(categoryId);
        Set<Long> visited = new HashSet<>();

        while (current != null && current.parent != null) {
            if (!visited.add(current.id)) {
                break;
            }
            depth++;
            current = current.parent;
        }

        return depth;
    }

    private int reassignStockItemsFromCategory(Long deletedCategoryId, Long newParentId, String newParentName) {
        if (newParentId == null) {
            return (int) StockItem.update(
                    "itemCategoryId = null, itemCategoryName = null where itemCategoryId = ?1",
                    deletedCategoryId);
        }

        return (int) StockItem.update(
                "itemCategoryId = ?1, itemCategoryName = ?2 where itemCategoryId = ?3",
                newParentId, newParentName, deletedCategoryId);
    }

    private int reassignActiveIngredientsFromCategory(Long deletedCategoryId, Long newParentId, String newParentName) {
        if (newParentId == null) {
            return (int) ActiveIngredient.update(
                    "categoryId = null, categoryName = null where categoryId = ?1",
                    deletedCategoryId);
        }

        return (int) ActiveIngredient.update(
                "categoryId = ?1, categoryName = ?2 where categoryId = ?3",
                newParentId, newParentName, deletedCategoryId);
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
