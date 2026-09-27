package org.example.finance.incomes.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.incomes.domains.IncomePaymentMethod;
import org.example.finance.incomes.domains.repositories.IncomePaymentMethodRepository;
import org.example.finance.incomes.services.payloads.requests.IncomePaymentMethodRequest;
import org.example.finance.incomes.services.payloads.responses.IncomePaymentMethodDto;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class IncomePaymentMethodService {

    @Inject
    IncomePaymentMethodRepository incomePaymentMethodRepository;

    @Transactional
    public Response createIncomePaymentMethod(IncomePaymentMethodRequest request) {
        if (request == null || request.methodName == null || request.methodName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Payment method name is required.", null))
                    .build();
        }
        IncomePaymentMethod method = new IncomePaymentMethod();
        method.methodName = request.methodName.trim();
        method.description = trimOrEmpty(request.description);
        method.dateCreated = LocalDate.now();
        method.dateUpdated = LocalDate.now();
        incomePaymentMethodRepository.persist(method);
        return Response.ok(new ResponseMessage("Payment method created successfully", new IncomePaymentMethodDto(method))).build();
    }

    @Transactional
    public List<IncomePaymentMethodDto> getAllIncomePaymentMethods() {
        if (incomePaymentMethodRepository.count() == 0) {
            seedDefaultMethods();
        }
        return incomePaymentMethodRepository.listAll(Sort.descending("id"))
                .stream()
                .map(IncomePaymentMethodDto::new)
                .toList();
    }

    private void seedDefaultMethods() {
        for (String name : List.of("Cash", "Bank Transfer", "Mobile Money", "Cheque", "Other")) {
            IncomePaymentMethod method = new IncomePaymentMethod();
            method.methodName = name;
            method.description = "";
            method.dateCreated = LocalDate.now();
            method.dateUpdated = LocalDate.now();
            incomePaymentMethodRepository.persist(method);
        }
    }

    @Transactional
    public Response updateIncomePaymentMethod(Long id, IncomePaymentMethodRequest request) {
        IncomePaymentMethod method = incomePaymentMethodRepository.findById(id);
        if (method == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Payment method not found", null))
                    .build();
        }
        if (request.methodName != null && !request.methodName.isBlank()) {
            method.methodName = request.methodName.trim();
        }
        if (request.description != null) {
            method.description = trimOrEmpty(request.description);
        }
        method.dateUpdated = LocalDate.now();
        incomePaymentMethodRepository.persist(method);
        return Response.ok(new ResponseMessage("Payment method updated successfully", new IncomePaymentMethodDto(method))).build();
    }

    @Transactional
    public Response deleteIncomePaymentMethod(Long id) {
        IncomePaymentMethod method = incomePaymentMethodRepository.findById(id);
        if (method == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Payment method not found", null))
                    .build();
        }
        incomePaymentMethodRepository.delete(method);
        return Response.ok(new ResponseMessage("Payment method deleted successfully")).build();
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
