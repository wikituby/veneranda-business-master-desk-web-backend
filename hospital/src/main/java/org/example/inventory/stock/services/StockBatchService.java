package org.example.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Category;
import org.example.inventory.item.domain.repositories.CategoryRepository;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockItem;
import org.example.treatment.domains.TreatmentRequested;
import org.example.inventory.stock.services.payloads.requests.StockBatchFieldsUpdateRequest;
import org.example.inventory.stock.services.payloads.requests.StockBatchRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockBatchDTO;
import org.example.inventory.stock.services.payloads.responses.dtos.UnitSellingModelDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class StockBatchService {

    @Inject
    UnitSellingModelService unitSellingModelService;

    @Inject
    CategoryRepository categoryRepository;

    @Transactional
    public Response createStockBatch(StockBatchRequest request) {

        StockBatch batch = new StockBatch();

        //batch.stockItemName = request.stockItemName;
        batch.stockItemId = request.stockItemId;
        batch.unitCostPrice = request.unitCostPrice;
        batch.unitSellingPrice = request.unitSellingPrice;
        //batch.stockAtHand = request.stockAtHand;
        batch.reOrderLevel = request.reOrderLevel;
        batch.reOrderTo = request.reOrderTo;
        batch.reOrderQuantity = request.reOrderQuantity;
        //batch.shelfNumber = request.shelfNumber;
        batch.unitOfMeasure = request.unitOfMeasure;
        batch.lastUnitValue = request.lastUnitValue;
        batch.lastUnitOfMeasure = request.lastUnitOfMeasure;
        batch.batchNumber = request.batchNumber;
        batch.stockSupplierId = request.stockSupplierId;
        batch.creationDateAndTime = LocalDateTime.now();
        batch.expiryDate = request.expiryDate;
        batch.persist();

        return Response.ok(new ResponseMessage("New stock Received successfully", new StockBatchDTO(batch))).build();

        //return batch;
    }

    @Transactional
    public List<StockBatchDTO> getAll(Long storeId) {
        List<StockBatch> batches;
        if (storeId != null) {
            batches = StockBatch.find("storeId = ?1", Sort.descending("id"), storeId).list();
        } else {
            batches = StockBatch.listAll(Sort.descending("id"));
        }
        return batches.stream()
                .map(this::toEnrichedDto)
                .collect(Collectors.toList());
    }

    private StockBatchDTO toEnrichedDto(StockBatch batch) {
        StockBatchDTO dto = new StockBatchDTO(batch);
        if (batch.stockItemId != null) {
            dto.unitSellingModels = unitSellingModelService.listByStockItem(batch.stockItemId, batch.unitCostPrice);
            if (dto.unitSellingModelId == null) {
                dto.unitSellingModelId = dto.unitSellingModels.stream()
                        .filter(m -> Boolean.TRUE.equals(m.isDefault))
                        .map(m -> m.id)
                        .findFirst()
                        .orElse(dto.unitSellingModels.isEmpty() ? null : dto.unitSellingModels.get(0).id);
            }
            enrichCategoryFields(dto, batch.stockItemId);
        }
        return dto;
    }

    private void enrichCategoryFields(StockBatchDTO dto, Long stockItemId) {
        StockItem stockItem = StockItem.findById(stockItemId);
        if (stockItem == null || stockItem.itemCategoryId == null) {
            return;
        }
        List<Category> categories = categoryRepository.listAll();
        if (categories.isEmpty()) {
            return;
        }
        Map<Long, Category> byId = categories.stream()
                .collect(Collectors.toMap(c -> c.id, c -> c, (a, b) -> a));
        Map<Long, Long> parentIdById = new HashMap<>();
        for (Category category : categories) {
            if (category.parent != null) {
                parentIdById.put(category.id, category.parent.id);
            }
        }
        Category category = byId.get(stockItem.itemCategoryId);
        if (category == null) {
            return;
        }
        Category topParent = resolveTopParent(category, parentIdById, byId);
        dto.lastCategoryId = topParent.id;
        dto.lastCategoryName = topParent.name;
    }

    private static Category resolveTopParent(
            Category category,
            Map<Long, Long> parentIdById,
            Map<Long, Category> byId) {
        Category current = category;
        Set<Long> visited = new HashSet<>();
        while (parentIdById.containsKey(current.id)) {
            if (!visited.add(current.id)) {
                break;
            }
            Long parentId = parentIdById.get(current.id);
            Category parent = byId.get(parentId);
            if (parent == null) {
                break;
            }
            current = parent;
        }
        return current;
    }

    public StockBatch getById(Long id) {
        return StockBatch.findById(id);
    }

    @Transactional
    public StockBatch updateStockBatch(Long id, StockBatchRequest request) {
        StockBatch stockBatch = StockBatch.findById(id);
        if (stockBatch == null)
            return null;

        //stockBatch.stockItemName = request.stockItemName;
        stockBatch.stockItemId = request.stockItemId;
        stockBatch.unitCostPrice = request.unitCostPrice;
        stockBatch.unitSellingPrice = request.unitSellingPrice;
        //stockBatch.stockAtHand = request.stockAtHand;
        stockBatch.reOrderLevel = request.reOrderLevel;
        stockBatch.reOrderTo = request.reOrderTo;
        stockBatch.reOrderQuantity = request.reOrderQuantity;
        //stockBatch.shelfNumber = request.shelfNumber;
        stockBatch.unitOfMeasure = request.unitOfMeasure;
        stockBatch.lastUnitValue = request.lastUnitValue;
        stockBatch.lastUnitOfMeasure = request.lastUnitOfMeasure;
        stockBatch.batchNumber = request.batchNumber;
        stockBatch.stockSupplierId = request.stockSupplierId;
        stockBatch.upDateDateAndTime = LocalDateTime.now();
        stockBatch.expiryDate = request.expiryDate;

        return stockBatch;
    }

    @Transactional
    public Response updateStockBatchFields(Long id, StockBatchFieldsUpdateRequest request) {
        StockBatch stockBatch = StockBatch.findById(id);
        if (stockBatch == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock batch not found for ID: " + id, null))
                    .build();
        }

        // Update the specified fields
        if (request.stockAtHand != null) {
            stockBatch.stockAtHand = request.stockAtHand;
        }
        if (request.unitCostPrice != null) {
            stockBatch.unitCostPrice = request.unitCostPrice;
        }
        if (request.unitSellingPrice != null) {
            stockBatch.unitSellingPrice = request.unitSellingPrice;
        }
        if (request.expiryDate != null) {
            stockBatch.expiryDate = request.expiryDate;
        }
        if (request.shelfNumber != null) {
            stockBatch.shelfNumber = request.shelfNumber;
        }
        if (request.profitMarginForRetail != null) {
            stockBatch.profitMarginForRetail = request.profitMarginForRetail;
        }
        if (request.profitMarginForWholeSale != null) {
            stockBatch.profitMarginForWholeSale = request.profitMarginForWholeSale;
        }
        if (request.profitMarginForSpecialCase != null) {
            stockBatch.profitMarginForSpecialCase = request.profitMarginForSpecialCase;
        }
        if (request.reOrderLevel != null) {
            stockBatch.reOrderLevel = request.reOrderLevel;
        }
        if (request.reOrderQuantity != null) {
            stockBatch.reOrderQuantity = request.reOrderQuantity;
        }

        if (request.reOrderTo != null) {
                stockBatch.reOrderTo = request.reOrderTo;

        }
        if (request.unitOfMeasure != null) {
            stockBatch.unitOfMeasure = request.unitOfMeasure;
        }
        if (request.lastUnitValue != null) {
            stockBatch.lastUnitValue = request.lastUnitValue;
        }
        if (request.lastUnitOfMeasure != null) {
            stockBatch.lastUnitOfMeasure = request.lastUnitOfMeasure;
        }
        if (request.unitSellingModelId != null) {
            stockBatch.unitSellingModelId = request.unitSellingModelId;
            BigDecimal modelPrice = unitSellingModelService.resolveUnitSellingPrice(
                    stockBatch.stockItemId,
                    request.unitSellingModelId,
                    stockBatch.unitSellingPrice
            );
            if (modelPrice != null && modelPrice.compareTo(BigDecimal.ZERO) > 0) {
                stockBatch.unitSellingPrice = modelPrice;
            }
        }

        stockBatch.upDateDateAndTime = LocalDateTime.now();
        stockBatch.persist();

        return Response.ok(new ResponseMessage("Stock batch fields updated successfully", toEnrichedDto(stockBatch))).build();
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    /**
     * Delete a stock batch only when it has no stock left and nothing in the system still depends on it.
     */
    @Transactional
    public Response deleteStockBatch(Long id) {
        StockBatch batch = StockBatch.findById(id);
        if (batch == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock batch not found for ID: " + id, null))
                    .build();
        }

        BigDecimal atHand = nz(batch.stockAtHand);
        if (atHand.compareTo(BigDecimal.ZERO) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "Cannot delete this stock batch: stock at hand is "
                                    + atHand.stripTrailingZeros().toPlainString()
                                    + ". Clear stock first (e.g. expiry register, transfer, or dispense).",
                            null))
                    .build();
        }

        if (TreatmentRequested.count("stockBatch.id = ?1", id) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "Cannot delete: this batch is linked to one or more treatment requests.", null))
                    .build();
        }

        batch.delete();
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label, null)).build();
    }
}
