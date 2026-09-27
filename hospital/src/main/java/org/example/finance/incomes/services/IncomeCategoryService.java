package org.example.finance.incomes.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.incomes.domains.IncomeCategory;
import org.example.finance.incomes.domains.repositories.IncomeCategoryRepository;
import org.example.finance.incomes.services.payloads.requests.IncomeCategoryRequest;
import org.example.finance.incomes.services.payloads.responses.IncomeCategoryDto;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class IncomeCategoryService {

    @Inject
    IncomeCategoryRepository incomeCategoryRepository;

    @Transactional
    public Response createIncomeCategory(IncomeCategoryRequest request) {
        if (request == null || request.categoryName == null || request.categoryName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Category name is required.", null))
                    .build();
        }
        IncomeCategory category = new IncomeCategory();
        category.categoryName = request.categoryName.trim();
        category.description = trimOrEmpty(request.description);
        category.dateOfCategoryCreation = LocalDate.now();
        category.dateOfCategoryUpdate = LocalDate.now();
        incomeCategoryRepository.persist(category);
        return Response.ok(new ResponseMessage("Income category created successfully", new IncomeCategoryDto(category))).build();
    }

    @Transactional
    public List<IncomeCategoryDto> getAllIncomeCategories() {
        return incomeCategoryRepository.listAll(Sort.descending("id"))
                .stream()
                .map(IncomeCategoryDto::new)
                .toList();
    }

    @Transactional
    public Response updateIncomeCategory(Long id, IncomeCategoryRequest request) {
        IncomeCategory category = incomeCategoryRepository.findById(id);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Income category not found", null))
                    .build();
        }
        if (request.categoryName != null && !request.categoryName.isBlank()) {
            category.categoryName = request.categoryName.trim();
        }
        if (request.description != null) {
            category.description = trimOrEmpty(request.description);
        }
        category.dateOfCategoryUpdate = LocalDate.now();
        incomeCategoryRepository.persist(category);
        return Response.ok(new ResponseMessage("Income category updated successfully", new IncomeCategoryDto(category))).build();
    }

    @Transactional
    public Response deleteIncomeCategory(Long id) {
        IncomeCategory category = incomeCategoryRepository.findById(id);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Income category not found", null))
                    .build();
        }
        incomeCategoryRepository.delete(category);
        return Response.ok(new ResponseMessage("Income category deleted successfully")).build();
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
