package org.example.finance.incomes.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.incomes.domains.IncomeAccount;
import org.example.finance.incomes.domains.IncomeCategory;
import org.example.finance.incomes.domains.repositories.IncomeAccountRepository;
import org.example.finance.incomes.domains.repositories.IncomeCategoryRepository;
import org.example.finance.incomes.services.payloads.requests.IncomeAccountRequest;
import org.example.finance.incomes.services.payloads.responses.IncomeAccountDto;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class IncomeAccountService {

    @Inject
    IncomeAccountRepository incomeAccountRepository;

    @Inject
    IncomeCategoryRepository incomeCategoryRepository;

    @Transactional
    public Response createIncomeAccount(IncomeAccountRequest request) {
        if (request == null || request.categoryId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Select an income category.", null))
                    .build();
        }
        if (request.accountName == null || request.accountName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Account name is required.", null))
                    .build();
        }
        IncomeCategory category = incomeCategoryRepository.findById(request.categoryId);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Income category not found.", request.categoryId))
                    .build();
        }
        IncomeAccount account = new IncomeAccount();
        account.category = category;
        account.accountName = request.accountName.trim();
        account.incomeCategoryName = category.categoryName;
        account.description = trimOrEmpty(request.description);
        account.dateOfAccountCreation = LocalDate.now();
        account.dateOfAccountUpdate = LocalDate.now();
        account.timeOfAccountCreation = java.time.LocalTime.now();
        incomeAccountRepository.persist(account);
        return Response.ok(new ResponseMessage("Income account created successfully", new IncomeAccountDto(account))).build();
    }

    @Transactional
    public List<IncomeAccountDto> getAllIncomeAccounts() {
        return incomeAccountRepository.listAll(Sort.descending("id"))
                .stream()
                .map(IncomeAccountDto::new)
                .toList();
    }

    @Transactional
    public Response updateIncomeAccount(Long id, IncomeAccountRequest request) {
        IncomeAccount account = incomeAccountRepository.findById(id);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Income account not found", null))
                    .build();
        }
        if (request.categoryId != null) {
            IncomeCategory category = incomeCategoryRepository.findById(request.categoryId);
            if (category == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Income category not found", request.categoryId))
                        .build();
            }
            account.category = category;
            account.incomeCategoryName = category.categoryName;
        }
        if (request.accountName != null && !request.accountName.isBlank()) {
            account.accountName = request.accountName.trim();
        }
        if (request.description != null) {
            account.description = trimOrEmpty(request.description);
        }
        account.dateOfAccountUpdate = LocalDate.now();
        incomeAccountRepository.persist(account);
        return Response.ok(new ResponseMessage("Income account updated successfully", new IncomeAccountDto(account))).build();
    }

    @Transactional
    public Response deleteIncomeAccount(Long id) {
        IncomeAccount account = incomeAccountRepository.findById(id);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Income account not found", null))
                    .build();
        }
        incomeAccountRepository.delete(account);
        return Response.ok(new ResponseMessage("Income account deleted successfully")).build();
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
