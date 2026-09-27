package org.example.finance.incomes.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.incomes.domains.IncomeSource;
import org.example.finance.incomes.domains.repositories.IncomeSourceRepository;
import org.example.finance.incomes.services.payloads.requests.IncomeSourceRequest;
import org.example.finance.incomes.services.payloads.responses.IncomeSourceDto;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class IncomeSourceService {

    @Inject
    IncomeSourceRepository incomeSourceRepository;

    @Transactional
    public Response createIncomeSource(IncomeSourceRequest request) {
        if (request == null || request.sourceName == null || request.sourceName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Source / payer name is required.", null))
                    .build();
        }
        IncomeSource source = new IncomeSource();
        source.sourceName = request.sourceName.trim();
        source.description = trimOrEmpty(request.description);
        source.dateCreated = LocalDate.now();
        source.dateUpdated = LocalDate.now();
        incomeSourceRepository.persist(source);
        return Response.ok(new ResponseMessage("Income source created successfully", new IncomeSourceDto(source))).build();
    }

    @Transactional
    public List<IncomeSourceDto> getAllIncomeSources() {
        return incomeSourceRepository.listAll(Sort.descending("id"))
                .stream()
                .map(IncomeSourceDto::new)
                .toList();
    }

    @Transactional
    public Response updateIncomeSource(Long id, IncomeSourceRequest request) {
        IncomeSource source = incomeSourceRepository.findById(id);
        if (source == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Income source not found", null))
                    .build();
        }
        if (request.sourceName != null && !request.sourceName.isBlank()) {
            source.sourceName = request.sourceName.trim();
        }
        if (request.description != null) {
            source.description = trimOrEmpty(request.description);
        }
        source.dateUpdated = LocalDate.now();
        incomeSourceRepository.persist(source);
        return Response.ok(new ResponseMessage("Income source updated successfully", new IncomeSourceDto(source))).build();
    }

    @Transactional
    public Response deleteIncomeSource(Long id) {
        IncomeSource source = incomeSourceRepository.findById(id);
        if (source == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Income source not found", null))
                    .build();
        }
        incomeSourceRepository.delete(source);
        return Response.ok(new ResponseMessage("Income source deleted successfully")).build();
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
