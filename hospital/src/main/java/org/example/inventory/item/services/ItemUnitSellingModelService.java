package org.example.inventory.item.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.ItemUnitSellingModel;
import org.example.inventory.item.domain.repositories.ItemUnitSellingModelRepository;
import org.example.inventory.item.services.payloads.responses.dtos.ItemUnitSellingModelDTO;
import org.example.inventory.stock.services.payloads.requests.UnitSellingModelRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ItemUnitSellingModelService {

    @Inject
    ItemUnitSellingModelRepository repository;

    @Transactional
    public List<ItemUnitSellingModelDTO> listByItem(Long itemId, BigDecimal unitCostPrice) {
        if (itemId == null) {
            return List.of();
        }
        ensureModelsForItem(itemId, unitCostPrice);
        return repository
                .list("item.id = ?1", Sort.by("sortOrder").and("id"), itemId)
                .stream()
                .map(ItemUnitSellingModelDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public BigDecimal resolveUnitSellingPrice(Long itemId, Long modelId, BigDecimal fallbackPrice) {
        if (modelId != null) {
            ItemUnitSellingModel model = repository.findById(modelId);
            if (model != null && model.unitSellingPrice != null) {
                return money(model.unitSellingPrice);
            }
        }
        if (itemId != null) {
            List<ItemUnitSellingModel> models = repository.list(
                    "item.id = ?1",
                    Sort.by("sortOrder").and("id"),
                    itemId
            );
            if (!models.isEmpty()) {
                ItemUnitSellingModel defaultModel = models.stream()
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
    public Response createModel(Long itemId, UnitSellingModelRequest request) {
        Item item = Item.findById(itemId);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Item not found for ID: " + itemId, null))
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
        if (nameExistsForItem(itemId, request.name, null)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("A unit sell model with this name already exists", null))
                    .build();
        }

        ItemUnitSellingModel model = new ItemUnitSellingModel();
        model.item = item;
        applyRequest(model, request);
        if (Boolean.TRUE.equals(request.isDefault)) {
            clearDefaultForItem(itemId);
        } else if (repository.count("item.id = ?1", itemId) == 0) {
            model.isDefault = true;
        }
        repository.persist(model);
        syncItemPriceFromModel(item, model);

        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, new ItemUnitSellingModelDTO(model))).build();
    }

    @Transactional
    public Response updateModel(Long modelId, UnitSellingModelRequest request) {
        ItemUnitSellingModel model = repository.findById(modelId);
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

        Long itemId = model.item != null ? model.item.id : null;
        if (itemId != null && nameExistsForItem(itemId, request.name, model.id)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("A unit sell model with this name already exists", null))
                    .build();
        }

        if (Boolean.TRUE.equals(request.isDefault) && itemId != null) {
            clearDefaultForItem(itemId);
        }
        applyRequest(model, request);
        repository.persist(model);
        if (model.item != null) {
            syncItemPriceFromModel(model.item, model);
        }

        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, new ItemUnitSellingModelDTO(model))).build();
    }

    @Transactional
    public Response deleteModel(Long modelId) {
        ItemUnitSellingModel model = repository.findById(modelId);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Unit selling model not found for ID: " + modelId, null))
                    .build();
        }
        Long itemId = model.item != null ? model.item.id : null;
        boolean wasDefault = Boolean.TRUE.equals(model.isDefault);
        repository.delete(model);
        if (itemId != null && wasDefault) {
            List<ItemUnitSellingModel> remaining = repository.list(
                    "item.id = ?1",
                    Sort.by("sortOrder").and("id"),
                    itemId
            );
            if (!remaining.isEmpty()) {
                ItemUnitSellingModel next = remaining.get(0);
                next.isDefault = true;
                repository.persist(next);
                if (next.item != null) {
                    syncItemPriceFromModel(next.item, next);
                }
            }
        }
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }

    @Transactional
    public void ensureModelsForItem(Long itemId, BigDecimal unitCostPrice) {
        if (itemId == null) {
            return;
        }
        long count = repository.count("item.id = ?1", itemId);
        if (count > 0) {
            return;
        }

        Item item = Item.findById(itemId);
        if (item == null) {
            return;
        }

        BigDecimal cost = money(unitCostPrice != null ? unitCostPrice : item.costPrice);
        BigDecimal sell = money(item.sellingPrice);
        if (sell.compareTo(BigDecimal.ZERO) <= 0) {
            sell = cost;
        }
        if (sell.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal margin = sell.compareTo(cost) > 0 ? sell.subtract(cost) : BigDecimal.ZERO;

        ItemUnitSellingModel retail = new ItemUnitSellingModel();
        retail.item = item;
        retail.name = "Retail";
        retail.unitSellingPrice = sell;
        retail.unitsInBundle = 1;
        retail.bundlePrice = sell;
        retail.profitMargin = margin;
        retail.sortOrder = 0;
        retail.isDefault = true;
        repository.persist(retail);

        item.unitSellingModelId = retail.id;
        item.sellingPrice = sell;
        item.persist();
    }

    private void applyRequest(ItemUnitSellingModel model, UnitSellingModelRequest request) {
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

    private void clearDefaultForItem(Long itemId) {
        List<ItemUnitSellingModel> models = repository.list("item.id = ?1", itemId);
        for (ItemUnitSellingModel existing : models) {
            existing.isDefault = false;
            repository.persist(existing);
        }
    }

    private void syncItemPriceFromModel(Item item, ItemUnitSellingModel model) {
        if (item == null || model == null) {
            return;
        }
        if (Boolean.TRUE.equals(model.isDefault) || item.unitSellingModelId == null) {
            item.unitSellingModelId = model.id;
            item.sellingPrice = money(model.unitSellingPrice);
            item.persist();
        } else if (item.unitSellingModelId.equals(model.id)) {
            item.sellingPrice = money(model.unitSellingPrice);
            item.persist();
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

    private boolean nameExistsForItem(Long itemId, String name, Long excludeModelId) {
        if (itemId == null || isBlank(name)) {
            return false;
        }
        String key = name.trim().toLowerCase();
        List<ItemUnitSellingModel> matches = repository.list(
                "item.id = ?1 and lower(name) = ?2",
                itemId,
                key
        );
        if (excludeModelId == null) {
            return !matches.isEmpty();
        }
        return matches.stream().anyMatch(m -> !excludeModelId.equals(m.id));
    }
}
