package org.example.inventory.item.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.ItemDosageRule;
import org.example.inventory.item.domain.repositories.ItemDosageRuleRepository;
import org.example.inventory.item.domain.repositories.ItemRepository;
import org.example.inventory.item.services.payloads.responses.dtos.ItemDosageRuleDTO;
import org.example.inventory.stock.services.payloads.requests.StockDosageRuleRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ItemDosageRuleService {

    @Inject
    ItemDosageRuleRepository itemDosageRuleRepository;

    @Inject
    ItemRepository itemRepository;

    @Transactional
    public Response createDosageRule(Long itemId, StockDosageRuleRequest request) {
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Item not found for ID: " + itemId, null))
                    .build();
        }
        if (request == null || isBlank(request.title)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("title is required", null))
                    .build();
        }

        ItemDosageRule rule = new ItemDosageRule();
        rule.item = item;
        applyRequest(rule, request);
        rule.creationDate = LocalDate.now();
        rule.updateDate = LocalDate.now();
        itemDosageRuleRepository.persist(rule);

        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, new ItemDosageRuleDTO(rule))).build();
    }

    @Transactional
    public List<ItemDosageRuleDTO> getDosageRulesByItem(Long itemId) {
        return itemDosageRuleRepository
                .list("item.id = ?1", Sort.by("sortOrder").and("id"), itemId)
                .stream()
                .map(ItemDosageRuleDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ItemDosageRuleDTO> getActiveDosageRulesByItem(Long itemId) {
        return itemDosageRuleRepository
                .list("item.id = ?1 and (active is null or active = true)", Sort.by("sortOrder").and("id"), itemId)
                .stream()
                .map(ItemDosageRuleDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response updateDosageRule(Long ruleId, StockDosageRuleRequest request) {
        ItemDosageRule rule = itemDosageRuleRepository.findById(ruleId);
        if (rule == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Dosage rule not found for ID: " + ruleId, null))
                    .build();
        }
        if (request == null || isBlank(request.title)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("title is required", null))
                    .build();
        }

        applyRequest(rule, request);
        rule.updateDate = LocalDate.now();
        itemDosageRuleRepository.persist(rule);

        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, new ItemDosageRuleDTO(rule))).build();
    }

    @Transactional
    public Response deleteDosageRule(Long ruleId) {
        ItemDosageRule rule = itemDosageRuleRepository.findById(ruleId);
        if (rule == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Dosage rule not found for ID: " + ruleId, null))
                    .build();
        }

        itemDosageRuleRepository.delete(rule);
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label, null)).build();
    }

    private void applyRequest(ItemDosageRule rule, StockDosageRuleRequest request) {
        rule.title = request.title;
        rule.matchLabel = request.matchLabel;
        rule.ruleLabel = request.ruleLabel;
        rule.doseCalculationType = request.doseCalculationType;
        rule.fixedDoseValue = request.fixedDoseValue;
        rule.fixedDoseUnit = request.fixedDoseUnit;
        rule.weightDoseValue = request.weightDoseValue;
        rule.weightDoseUnit = request.weightDoseUnit;
        rule.maxDoseValue = request.maxDoseValue;
        rule.maxDoseUnit = request.maxDoseUnit;
        rule.minAgeYears = request.minAgeYears;
        rule.maxAgeYears = request.maxAgeYears;
        rule.minWeightKg = request.minWeightKg;
        rule.maxWeightKg = request.maxWeightKg;
        rule.route = request.route;
        rule.frequencyValue = request.frequencyValue;
        rule.frequencyUnit = request.frequencyUnit;
        rule.durationValue = request.durationValue;
        rule.durationUnit = request.durationUnit;
        rule.quantity = request.quantity;
        rule.quantityUnit = request.quantityUnit;
        rule.stockUnitsPerDose = request.stockUnitsPerDose;
        rule.stockUnitLabel = request.stockUnitLabel;
        rule.instructions = request.instructions;
        rule.specialScheduleText = request.specialScheduleText;
        rule.warning = request.warning;
        rule.active = request.active != null ? request.active : true;
        rule.sortOrder = request.sortOrder != null ? request.sortOrder : 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
