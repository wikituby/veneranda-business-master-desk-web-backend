package org.example.finance.expenses.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.expenses.domains.ExpenseCategory;
import org.example.finance.expenses.domains.repositories.ExpenseCategoryRepository;
import org.example.finance.expenses.services.payloads.requests.ExpenseCategoryRequest;
import org.example.finance.expenses.services.payloads.responses.ExpenseCategoryDto;
import org.example.finance.expenses.services.payloads.responses.ExpenseTransactionDto;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ExpenseCategoryService {


    @Inject
    ExpenseCategoryRepository expenseCategoryRepository;

    public Response createExpenseCategory(ExpenseCategoryRequest request){

        ExpenseCategory expenseCategory = new ExpenseCategory();
        expenseCategory.categoryName = request.categoryName;
        expenseCategory.description = request.description;
        expenseCategory.dateOfCategoryCreation = LocalDate.now();
        expenseCategory.dateOfCategoryUpdate = LocalDate.now();

        expenseCategoryRepository.persist(expenseCategory);

        return Response.ok(new ResponseMessage("New Expense Category created successfully", new ExpenseCategoryDto(expenseCategory))).build();

    }


    @Transactional
    public List<ExpenseCategoryDto> getAllExpenseCategory() {
        return expenseCategoryRepository.listAll(Sort.descending("id"))
                .stream()
                .map(ExpenseCategoryDto::new)
                .toList();
    }
}






