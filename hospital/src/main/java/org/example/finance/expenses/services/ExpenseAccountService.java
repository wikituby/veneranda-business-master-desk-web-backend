package org.example.finance.expenses.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.expenses.domains.ExpenseAccount;
import org.example.finance.expenses.domains.ExpenseCategory;
import org.example.finance.expenses.domains.repositories.ExpenseAccountRepository;
import org.example.finance.expenses.domains.repositories.ExpenseCategoryRepository;
import org.example.finance.expenses.services.payloads.requests.ExpenseAccountRequest;
import org.example.finance.expenses.services.payloads.responses.ExpenseAccountDto;
import org.example.finance.expenses.services.payloads.responses.ExpenseTransactionDto;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ExpenseAccountService {

    @Inject
    ExpenseAccountRepository expenseAccountRepository;

    @Inject
    ExpenseCategoryRepository expenseCategoryRepository;



    public Response createExpenseAccount(ExpenseAccountRequest request){

        ExpenseCategory expenseCategory = expenseCategoryRepository.findById(request.categoryId);
        if (expenseCategory == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("category not found for ID.", request.categoryId))
                    .build();
        }

        ExpenseAccount expenseAccount = new ExpenseAccount();
        expenseAccount.category = expenseCategory;
        expenseAccount.accountName = request.accountName;
        expenseAccount.description = request.description;
        expenseAccount.expenseCategoryName = expenseCategory.categoryName;
        expenseAccount.accountLimit = request.accountLimit;
        expenseAccount.limitPeriod = request.limitPeriod;
        expenseAccount.dateOfAccountCreation = LocalDate.now();
        expenseAccount.dateOfAccountUpdate = LocalDate.now();
        expenseAccount.timeOfAccountCreation = java.time.LocalTime.now();

        expenseAccountRepository.persist(expenseAccount);

        return Response.ok(new ResponseMessage("New Expense Account created successfully", new ExpenseAccountDto(expenseAccount))).build();

    }


    @Transactional
    public List<ExpenseAccountDto> getAllExpenseAccount() {
        return expenseAccountRepository.listAll(Sort.descending("id"))
                .stream()
                .map(ExpenseAccountDto::new)
                .toList();
    }

    @Transactional
    public Response updateExpenseAccount(Long id, ExpenseAccountRequest request) {
        ExpenseAccount account = expenseAccountRepository.findById(id);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Expense account not found", id))
                    .build();
        }

        if (request.categoryId != null) {
            ExpenseCategory category = expenseCategoryRepository.findById(request.categoryId);
            if (category == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Category not found", request.categoryId))
                        .build();
            }
            account.category = category;
            account.expenseCategoryName = category.categoryName;
        }

        if (request.accountName != null) account.accountName = request.accountName;
        if (request.description != null) account.description = request.description;
        account.accountLimit = request.accountLimit;
        account.limitPeriod = request.limitPeriod;
        account.dateOfAccountUpdate = LocalDate.now();

        expenseAccountRepository.persist(account);
        return Response.ok(new ResponseMessage("Expense account updated successfully", new ExpenseAccountDto(account))).build();
    }

    @Transactional
    public Response deleteExpenseAccount(Long id) {
        ExpenseAccount account = expenseAccountRepository.findById(id);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Expense account not found", id))
                    .build();
        }
        expenseAccountRepository.delete(account);
        return Response.ok(new ResponseMessage("Expense account deleted successfully", id)).build();
    }
}






