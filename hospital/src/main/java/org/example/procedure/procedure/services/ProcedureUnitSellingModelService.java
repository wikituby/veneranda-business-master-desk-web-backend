package org.example.procedure.procedure.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.stock.services.payloads.requests.UnitSellingModelRequest;
import org.example.procedure.procedure.domains.Procedure;
import org.example.procedure.procedure.domains.ProcedureUnitSellingModel;
import org.example.procedure.procedure.domains.repositories.ProcedureUnitSellingModelRepository;
import org.example.procedure.procedure.services.payloads.responses.dtos.ProcedureUnitSellingModelDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProcedureUnitSellingModelService {

    @Inject
    ProcedureUnitSellingModelRepository repository;

    @Transactional
    public List<ProcedureUnitSellingModelDTO> listByProcedure(Long procedureId, BigDecimal unitCostPrice) {
        if (procedureId == null) {
            return List.of();
        }
        ensureModelsForProcedure(procedureId, unitCostPrice);
        return repository
                .list("procedure.id = ?1", Sort.by("sortOrder").and("id"), procedureId)
                .stream()
                .map(ProcedureUnitSellingModelDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public BigDecimal resolveUnitSellingPrice(Long procedureId, Long modelId, BigDecimal fallbackPrice) {
        if (modelId != null) {
            ProcedureUnitSellingModel model = repository.findById(modelId);
            if (model != null && model.unitSellingPrice != null) {
                return money(model.unitSellingPrice);
            }
        }
        if (procedureId != null) {
            List<ProcedureUnitSellingModel> models = repository.list(
                    "procedure.id = ?1",
                    Sort.by("sortOrder").and("id"),
                    procedureId
            );
            if (!models.isEmpty()) {
                ProcedureUnitSellingModel defaultModel = models.stream()
                        .filter(m -> Boolean.TRUE.equals(m.isDefault))
                        .findFirst()
                        .orElse(models.get(0));
                if (defaultModel.unitSellingPrice != null) {
                    return money(defaultModel.unitSellingPrice);
                }
            }
        }
        return money(fallbackPrice);
    }

    @Transactional
    public Response createModel(Long procedureId, UnitSellingModelRequest request) {
        Procedure procedure = Procedure.findById(procedureId);
        if (procedure == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Service not found for ID: " + procedureId, null))
                    .build();
        }
        if (request == null || isBlank(request.name)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("name is required", null))
                    .build();
        }
        if (request.unitSellingPrice == null
                && (request.bundlePrice == null
                || request.unitsInBundle == null
                || request.unitsInBundle <= 0)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("unitSellingPrice or bundle pricing is required", null))
                    .build();
        }
        if (nameExistsForProcedure(procedureId, request.name, null)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("A unit sell model with this name already exists", null))
                    .build();
        }

        ProcedureUnitSellingModel model = new ProcedureUnitSellingModel();
        model.procedure = procedure;
        applyRequest(model, request);
        if (Boolean.TRUE.equals(request.isDefault)) {
            clearDefaultForProcedure(procedureId);
        } else if (repository.count("procedure.id = ?1", procedureId) == 0) {
            model.isDefault = true;
        }
        repository.persist(model);
        syncProcedurePriceFromModel(procedure, model);

        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, new ProcedureUnitSellingModelDTO(model))).build();
    }

    @Transactional
    public ProcedureUnitSellingModel createModelEntity(Long procedureId, UnitSellingModelRequest request) {
        Procedure procedure = Procedure.findById(procedureId);
        if (procedure == null) {
            return null;
        }
        ProcedureUnitSellingModel model = new ProcedureUnitSellingModel();
        model.procedure = procedure;
        applyRequest(model, request != null ? request : new UnitSellingModelRequest());
        if (request == null || request.isDefault == null || !request.isDefault) {
            if (repository.count("procedure.id = ?1", procedureId) == 0) {
                model.isDefault = true;
            }
        } else if (Boolean.TRUE.equals(request.isDefault)) {
            clearDefaultForProcedure(procedureId);
            model.isDefault = true;
        }
        repository.persist(model);
        syncProcedurePriceFromModel(procedure, model);
        return model;
    }

    @Transactional
    public Response updateModel(Long modelId, UnitSellingModelRequest request) {
        ProcedureUnitSellingModel model = repository.findById(modelId);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Unit selling model not found for ID: " + modelId, null))
                    .build();
        }
        if (request == null || isBlank(request.name)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("name is required", null))
                    .build();
        }

        Long procedureId = model.procedure != null ? model.procedure.id : null;
        if (procedureId != null && nameExistsForProcedure(procedureId, request.name, model.id)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("A unit sell model with this name already exists", null))
                    .build();
        }

        if (Boolean.TRUE.equals(request.isDefault) && procedureId != null) {
            clearDefaultForProcedure(procedureId);
        }
        applyRequest(model, request);
        repository.persist(model);
        if (model.procedure != null) {
            syncProcedurePriceFromModel(model.procedure, model);
        }

        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, new ProcedureUnitSellingModelDTO(model))).build();
    }

    @Transactional
    public Response deleteModel(Long modelId) {
        ProcedureUnitSellingModel model = repository.findById(modelId);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Unit selling model not found for ID: " + modelId, null))
                    .build();
        }
        Long procedureId = model.procedure != null ? model.procedure.id : null;
        boolean wasDefault = Boolean.TRUE.equals(model.isDefault);
        repository.delete(model);
        if (procedureId != null && wasDefault) {
            List<ProcedureUnitSellingModel> remaining = repository.list(
                    "procedure.id = ?1",
                    Sort.by("sortOrder").and("id"),
                    procedureId
            );
            if (!remaining.isEmpty()) {
                ProcedureUnitSellingModel next = remaining.get(0);
                next.isDefault = true;
                repository.persist(next);
                if (next.procedure != null) {
                    syncProcedurePriceFromModel(next.procedure, next);
                }
            }
        }
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }

    @Transactional
    public void ensureModelsForProcedure(Long procedureId, BigDecimal unitCostPrice) {
        if (procedureId == null) {
            return;
        }
        long count = repository.count("procedure.id = ?1", procedureId);
        if (count > 0) {
            return;
        }

        Procedure procedure = Procedure.findById(procedureId);
        if (procedure == null) {
            return;
        }

        BigDecimal cost = money(unitCostPrice != null ? unitCostPrice : procedure.unitCostPrice);
        BigDecimal sell = money(procedure.unitSellingPrice);
        if (sell.compareTo(BigDecimal.ZERO) <= 0) {
            sell = cost;
        }
        BigDecimal margin = sell.compareTo(cost) > 0 ? sell.subtract(cost) : BigDecimal.ZERO;

        ProcedureUnitSellingModel retail = new ProcedureUnitSellingModel();
        retail.procedure = procedure;
        retail.name = "Retail";
        retail.unitSellingPrice = sell;
        retail.unitsInBundle = 1;
        retail.bundlePrice = sell;
        retail.profitMargin = margin;
        retail.sortOrder = 0;
        retail.isDefault = true;
        repository.persist(retail);

        procedure.unitSellingModelId = retail.id;
        procedure.unitSellingPrice = sell;
        procedure.persist();
    }

    private void applyRequest(ProcedureUnitSellingModel model, UnitSellingModelRequest request) {
        model.name = request.name == null ? model.name : request.name.trim();
        Integer units = request.unitsInBundle;
        BigDecimal bundle = request.bundlePrice != null ? money(request.bundlePrice) : null;
        if (units != null && units > 0 && bundle != null && bundle.compareTo(BigDecimal.ZERO) > 0) {
            model.unitsInBundle = units;
            model.bundlePrice = bundle;
            model.unitSellingPrice = money(bundle.divide(
                    BigDecimal.valueOf(units),
                    2,
                    RoundingMode.HALF_UP
            ));
        } else if (request.unitSellingPrice != null) {
            model.unitSellingPrice = money(request.unitSellingPrice);
            int bundleUnits = units != null && units > 0 ? units : 1;
            model.unitsInBundle = bundleUnits;
            model.bundlePrice = bundle != null && bundle.compareTo(BigDecimal.ZERO) > 0
                    ? bundle
                    : money(model.unitSellingPrice.multiply(BigDecimal.valueOf(bundleUnits)));
        }
        model.profitMargin = request.profitMargin != null ? money(request.profitMargin) : model.profitMargin;
        model.sortOrder = request.sortOrder;
        if (request.isDefault != null) {
            model.isDefault = request.isDefault;
        }
    }

    private void clearDefaultForProcedure(Long procedureId) {
        List<ProcedureUnitSellingModel> models = repository.list("procedure.id = ?1", procedureId);
        for (ProcedureUnitSellingModel existing : models) {
            existing.isDefault = false;
            repository.persist(existing);
        }
    }

    private void syncProcedurePriceFromModel(Procedure procedure, ProcedureUnitSellingModel model) {
        if (procedure == null || model == null) {
            return;
        }
        if (Boolean.TRUE.equals(model.isDefault) || procedure.unitSellingModelId == null) {
            procedure.unitSellingModelId = model.id;
            procedure.unitSellingPrice = money(model.unitSellingPrice);
            procedure.persist();
        } else if (procedure.unitSellingModelId.equals(model.id)) {
            procedure.unitSellingPrice = money(model.unitSellingPrice);
            procedure.persist();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean nameExistsForProcedure(Long procedureId, String name, Long excludeModelId) {
        if (procedureId == null || isBlank(name)) {
            return false;
        }
        String key = name.trim().toLowerCase();
        List<ProcedureUnitSellingModel> matches = repository.list(
                "procedure.id = ?1 and lower(name) = ?2",
                procedureId,
                key
        );
        if (excludeModelId == null) {
            return !matches.isEmpty();
        }
        return matches.stream().anyMatch(m -> !excludeModelId.equals(m.id));
    }
}
