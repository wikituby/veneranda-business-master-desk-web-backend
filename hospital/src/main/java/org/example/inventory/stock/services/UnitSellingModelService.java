package org.example.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockItem;
import org.example.inventory.stock.domains.UnitSellingModel;
import org.example.inventory.stock.domains.repositories.UnitSellingModelRepository;
import org.example.inventory.stock.services.payloads.requests.UnitSellingModelRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.UnitSellingModelDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class UnitSellingModelService {

    @Inject
    UnitSellingModelRepository unitSellingModelRepository;

    @Transactional
    public List<UnitSellingModelDTO> listByStockItem(Long stockItemId, BigDecimal unitCostPrice) {
        if (stockItemId == null) {
            return List.of();
        }
        ensureModelsForStockItem(stockItemId, unitCostPrice);
        return unitSellingModelRepository
                .list("stockItem.id = ?1", Sort.by("sortOrder").and("id"), stockItemId)
                .stream()
                .map(UnitSellingModelDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public UnitSellingModelDTO resolveModel(Long modelId) {
        if (modelId == null) {
            return null;
        }
        UnitSellingModel model = unitSellingModelRepository.findById(modelId);
        return model == null ? null : new UnitSellingModelDTO(model);
    }

    @Transactional
    public BigDecimal resolveUnitSellingPrice(Long stockItemId, Long modelId, BigDecimal fallbackPrice) {
        if (modelId != null) {
            UnitSellingModel model = unitSellingModelRepository.findById(modelId);
            if (model != null && model.unitSellingPrice != null) {
                return money(model.unitSellingPrice);
            }
        }
        if (stockItemId != null) {
            List<UnitSellingModel> models = unitSellingModelRepository.list(
                    "stockItem.id = ?1",
                    Sort.by("sortOrder").and("id"),
                    stockItemId
            );
            if (!models.isEmpty()) {
                UnitSellingModel defaultModel = models.stream()
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
    public Response createModel(Long stockItemId, UnitSellingModelRequest request) {
        StockItem stockItem = StockItem.findById(stockItemId);
        if (stockItem == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock item not found for ID: " + stockItemId, null))
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
        if (nameExistsForStockItem(stockItemId, request.name, null)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("A unit sell model with this name already exists", null))
                    .build();
        }

        UnitSellingModel model = new UnitSellingModel();
        model.stockItem = stockItem;
        applyRequest(model, request);
        if (Boolean.TRUE.equals(request.isDefault)) {
            clearDefaultForStockItem(stockItemId);
        } else if (unitSellingModelRepository.count("stockItem.id = ?1", stockItemId) == 0) {
            model.isDefault = true;
        }
        unitSellingModelRepository.persist(model);
        syncBatchDefaultPriceFromModel(stockItemId, model);

        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, new UnitSellingModelDTO(model))).build();
    }

    @Transactional
    public Response updateModel(Long modelId, UnitSellingModelRequest request) {
        UnitSellingModel model = unitSellingModelRepository.findById(modelId);
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
        if (request.unitSellingPrice == null
                && (request.bundlePrice == null
                || request.unitsInBundle == null
                || request.unitsInBundle <= 0)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("unitSellingPrice or bundle pricing is required", null))
                    .build();
        }

        Long stockItemId = model.stockItem != null ? model.stockItem.id : null;
        if (stockItemId != null && nameExistsForStockItem(stockItemId, request.name, model.id)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("A unit sell model with this name already exists", null))
                    .build();
        }

        if (Boolean.TRUE.equals(request.isDefault) && stockItemId != null) {
            clearDefaultForStockItem(stockItemId);
        }
        applyRequest(model, request);
        unitSellingModelRepository.persist(model);
        if (stockItemId != null) {
            syncBatchDefaultPriceFromModel(stockItemId, model);
        }

        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, new UnitSellingModelDTO(model))).build();
    }

    @Transactional
    public Response deleteModel(Long modelId) {
        UnitSellingModel model = unitSellingModelRepository.findById(modelId);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Unit selling model not found for ID: " + modelId, null))
                    .build();
        }
        Long stockItemId = model.stockItem != null ? model.stockItem.id : null;
        boolean wasDefault = Boolean.TRUE.equals(model.isDefault);
        unitSellingModelRepository.delete(model);
        if (stockItemId != null && wasDefault) {
            List<UnitSellingModel> remaining = unitSellingModelRepository.list(
                    "stockItem.id = ?1",
                    Sort.by("sortOrder").and("id"),
                    stockItemId
            );
            if (!remaining.isEmpty()) {
                UnitSellingModel next = remaining.get(0);
                next.isDefault = true;
                unitSellingModelRepository.persist(next);
                syncBatchDefaultPriceFromModel(stockItemId, next);
            }
        }
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }

    @Transactional
    public void ensureModelsForStockItem(Long stockItemId, BigDecimal unitCostPrice) {
        if (stockItemId == null) {
            return;
        }
        long count = unitSellingModelRepository.count("stockItem.id = ?1", stockItemId);
        if (count > 0) {
            return;
        }

        StockBatch latestBatch = StockBatch.find(
                "stockItemId = ?1 ORDER BY id DESC",
                stockItemId
        ).firstResult();

        List<UnitSellingModel> seeded = new ArrayList<>();
        if (latestBatch != null) {
            BigDecimal cost = money(unitCostPrice != null ? unitCostPrice : latestBatch.unitCostPrice);
            int order = 0;
            if (hasPositive(latestBatch.profitMarginForRetail) || hasPositive(latestBatch.unitSellingPrice)) {
                seeded.add(buildSeedModel(stockItemId, "Retail", latestBatch.unitSellingPrice, latestBatch.profitMarginForRetail, cost, order++, true));
            }
            if (hasPositive(latestBatch.profitMarginForWholeSale)) {
                BigDecimal sell = cost.add(money(latestBatch.profitMarginForWholeSale));
                seeded.add(buildSeedModel(stockItemId, "Wholesale", sell, latestBatch.profitMarginForWholeSale, cost, order++, seeded.isEmpty()));
            }
            if (hasPositive(latestBatch.profitMarginForSpecialCase)) {
                BigDecimal sell = cost.add(money(latestBatch.profitMarginForSpecialCase));
                seeded.add(buildSeedModel(stockItemId, "Special", sell, latestBatch.profitMarginForSpecialCase, cost, order++, seeded.isEmpty()));
            }
        }

        if (seeded.isEmpty()) {
            BigDecimal cost = money(unitCostPrice);
            UnitSellingModel retail = buildSeedModel(stockItemId, "Retail", cost, BigDecimal.ZERO, cost, 0, true);
            seeded.add(retail);
        }

        StockItem stockItem = StockItem.findById(stockItemId);
        if (stockItem == null) {
            return;
        }
        for (UnitSellingModel model : seeded) {
            model.stockItem = stockItem;
            unitSellingModelRepository.persist(model);
        }
    }

    private UnitSellingModel buildSeedModel(
            Long stockItemId,
            String name,
            BigDecimal unitSellingPrice,
            BigDecimal profitMargin,
            BigDecimal unitCostPrice,
            int sortOrder,
            boolean isDefault
    ) {
        UnitSellingModel model = new UnitSellingModel();
        model.name = name;
        BigDecimal sell = money(unitSellingPrice);
        BigDecimal margin = money(profitMargin);
        if (margin.compareTo(BigDecimal.ZERO) <= 0 && sell.compareTo(unitCostPrice) > 0) {
            margin = sell.subtract(unitCostPrice).max(BigDecimal.ZERO);
        }
        if (sell.compareTo(BigDecimal.ZERO) <= 0 && margin.compareTo(BigDecimal.ZERO) > 0) {
            sell = unitCostPrice.add(margin);
        }
        model.unitSellingPrice = sell;
        model.unitsInBundle = 1;
        model.bundlePrice = sell;
        model.profitMargin = margin;
        model.sortOrder = sortOrder;
        model.isDefault = isDefault;
        return model;
    }

    private void applyRequest(UnitSellingModel model, UnitSellingModelRequest request) {
        model.name = request.name.trim();
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
        model.profitMargin = request.profitMargin != null ? money(request.profitMargin) : null;
        model.sortOrder = request.sortOrder;
        if (request.isDefault != null) {
            model.isDefault = request.isDefault;
        }
    }

    private void clearDefaultForStockItem(Long stockItemId) {
        List<UnitSellingModel> models = unitSellingModelRepository.list("stockItem.id = ?1", stockItemId);
        for (UnitSellingModel existing : models) {
            existing.isDefault = false;
            unitSellingModelRepository.persist(existing);
        }
    }

    private void syncBatchDefaultPriceFromModel(Long stockItemId, UnitSellingModel model) {
        if (!Boolean.TRUE.equals(model.isDefault) || stockItemId == null) {
            return;
        }
        List<StockBatch> batches = StockBatch.list("stockItemId = ?1", stockItemId);
        for (StockBatch batch : batches) {
            batch.unitSellingPrice = money(model.unitSellingPrice);
            if ("Retail".equalsIgnoreCase(model.name)) {
                batch.profitMarginForRetail = money(model.profitMargin);
            } else if ("Wholesale".equalsIgnoreCase(model.name)) {
                batch.profitMarginForWholeSale = money(model.profitMargin);
            } else if ("Special".equalsIgnoreCase(model.name)) {
                batch.profitMarginForSpecialCase = money(model.profitMargin);
            }
            batch.persist();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean hasPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private static BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean nameExistsForStockItem(Long stockItemId, String name, Long excludeModelId) {
        if (stockItemId == null || isBlank(name)) {
            return false;
        }
        String key = name.trim().toLowerCase();
        List<UnitSellingModel> matches = unitSellingModelRepository.list(
                "stockItem.id = ?1 and lower(name) = ?2",
                stockItemId,
                key
        );
        if (excludeModelId == null) {
            return !matches.isEmpty();
        }
        return matches.stream().anyMatch(m -> !excludeModelId.equals(m.id));
    }
}
