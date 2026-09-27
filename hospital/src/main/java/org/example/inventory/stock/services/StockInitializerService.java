package org.example.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.repositories.ItemRepository;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockInitialization;
import org.example.inventory.stock.domains.StockItem;
import org.example.inventory.stock.domains.repositories.StockBatchRepository;
import org.example.inventory.stock.domains.repositories.StockInitializationRepository;
import org.example.inventory.stock.domains.repositories.StockItemRepository;
import org.example.inventory.stock.services.payloads.requests.StockInitializerRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockInitializationDTO;
import org.example.inventory.stock.services.payloads.responses.dtos.StockInitializerDashboardDTO;
import org.example.inventory.store.domains.Store;
import org.example.inventory.store.domains.repositories.StoreRepository;
import org.example.subscription.domains.repositories.FacilityBusinessSettingsRepository;
import org.example.subscription.domains.repositories.FacilitySubscriptionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class StockInitializerService {

    public static final String REF_STOCK_INITIALIZER = "StockInitialization";

    @Inject StockInitializationRepository stockInitializationRepository;
    @Inject StockBatchRepository stockBatchRepository;
    @Inject StockItemRepository stockItemRepository;
    @Inject ItemRepository itemRepository;
    @Inject StoreRepository storeRepository;
    @Inject StockTrackingService stockTrackingService;
    @Inject FacilityBusinessSettingsRepository businessSettingsRepository;
    @Inject FacilitySubscriptionRepository facilitySubscriptionRepository;

    public boolean resolveUseStockBatch() {
        Long facilityId = facilitySubscriptionRepository
                .find("order by id desc")
                .firstResultOptional()
                .map(s -> s.facilityId)
                .orElse(null);
        if (facilityId == null) {
            return true;
        }
        return businessSettingsRepository.findByFacilityId(facilityId)
                .map(s -> s.pharmacyUseStockBatch == null || Boolean.TRUE.equals(s.pharmacyUseStockBatch))
                .orElse(true);
    }

    @Transactional
    public StockInitializerDashboardDTO getDashboard(Long storeId) {
        boolean useStockBatch = resolveUseStockBatch();
        StockInitializerDashboardDTO dashboard = new StockInitializerDashboardDTO();
        dashboard.useStockBatch = useStockBatch;

        List<StockInitialization> allRecords = stockInitializationRepository.listAll(Sort.descending("updatedAt", "id"));
        Map<String, StockInitialization> draftByKey = new HashMap<>();
        List<StockInitializationDTO> initialized = new ArrayList<>();

        for (StockInitialization record : allRecords) {
            if (record.finalized) {
                StockInitializationDTO dto = StockInitializationDTO.fromRecord(record);
                enrichCurrentStock(dto, useStockBatch);
                initialized.add(dto);
            } else {
                draftByKey.put(recordKey(record, useStockBatch), record);
            }
        }

        Set<String> finalizedKeys = allRecords.stream()
                .filter(r -> r.finalized)
                .map(r -> recordKey(r, useStockBatch))
                .collect(Collectors.toSet());

        List<StockInitializerDashboardDTO.StockInitializerPendingDTO> pending = new ArrayList<>();

        if (useStockBatch) {
            pending.addAll(buildBatchPending(storeId, finalizedKeys, draftByKey));
        } else {
            pending.addAll(buildShopPending(finalizedKeys, draftByKey));
        }

        dashboard.pending = pending;
        dashboard.initialized = initialized;
        dashboard.pendingCount = pending.size();
        dashboard.initializedCount = initialized.size();
        return dashboard;
    }

    private List<StockInitializerDashboardDTO.StockInitializerPendingDTO> buildBatchPending(
            Long storeId,
            Set<String> finalizedKeys,
            Map<String, StockInitialization> draftByKey) {

        List<StockInitializerDashboardDTO.StockInitializerPendingDTO> pending = new ArrayList<>();
        Set<Long> seenBatchIds = new HashSet<>();
        Set<String> seenCatalogKeys = new HashSet<>();

        String batchQuery = storeId != null ? "storeId = ?1" : "1=1";
        Object[] batchParams = storeId != null ? new Object[]{storeId} : new Object[]{};
        List<StockBatch> batches = stockBatchRepository.list(batchQuery, batchParams);

        for (StockBatch batch : batches) {
            String key = batchKey(batch.id, null, batch.storeId);
            if (finalizedKeys.contains(key)) {
                continue;
            }
            seenBatchIds.add(batch.id);
            pending.add(toPendingBatch(batch, draftByKey.get(key)));
        }

        List<StockItem> stockItems = stockItemRepository.listAll(Sort.ascending("stockItemName", "id"));
        List<Store> stores = storeId != null
                ? storeRepository.list("id = ?1", storeId)
                : storeRepository.listAll(Sort.ascending("name", "id"));

        for (StockItem stockItem : stockItems) {
            for (Store store : stores) {
                String catalogKey = catalogKey(stockItem.id, store.id);
                if (finalizedKeys.contains(catalogKey) || seenCatalogKeys.contains(catalogKey)) {
                    continue;
                }
                boolean hasBatch = batches.stream().anyMatch(b ->
                        Objects.equals(b.stockItemId, stockItem.id) && Objects.equals(b.storeId, store.id));
                if (hasBatch) {
                    continue;
                }
                seenCatalogKeys.add(catalogKey);
                StockInitializerDashboardDTO.StockInitializerPendingDTO row =
                        new StockInitializerDashboardDTO.StockInitializerPendingDTO();
                row.stockItemId = stockItem.id;
                row.itemName = stockItem.stockItemName;
                row.storeId = store.id;
                row.storeName = store.name;
                row.currentStockAtHand = BigDecimal.ZERO;
                StockInitialization draft = draftByKey.get(catalogKey);
                if (draft != null) {
                    row.draftQuantity = draft.draftQuantity;
                    row.draftId = draft.id;
                }
                pending.add(row);
            }
        }

        pending.sort(Comparator.comparing(r -> r.itemName != null ? r.itemName.toLowerCase() : ""));
        return pending;
    }

    private List<StockInitializerDashboardDTO.StockInitializerPendingDTO> buildShopPending(
            Set<String> finalizedKeys,
            Map<String, StockInitialization> draftByKey) {

        List<StockInitializerDashboardDTO.StockInitializerPendingDTO> pending = new ArrayList<>();
        List<Item> items = itemRepository.listAll(Sort.ascending("title", "id"));

        for (Item item : items) {
            String key = shopKey(item.id);
            if (finalizedKeys.contains(key)) {
                continue;
            }
            StockInitializerDashboardDTO.StockInitializerPendingDTO row =
                    new StockInitializerDashboardDTO.StockInitializerPendingDTO();
            row.itemId = item.id;
            row.itemName = item.title;
            row.currentStockAtHand = nz(item.stockAtHand);
            StockInitialization draft = draftByKey.get(key);
            if (draft != null) {
                row.draftQuantity = draft.draftQuantity;
                row.draftId = draft.id;
            }
            pending.add(row);
        }
        return pending;
    }

    private StockInitializerDashboardDTO.StockInitializerPendingDTO toPendingBatch(
            StockBatch batch,
            StockInitialization draft) {
        StockInitializerDashboardDTO.StockInitializerPendingDTO row =
                new StockInitializerDashboardDTO.StockInitializerPendingDTO();
        row.stockBatchId = batch.id;
        row.stockItemId = batch.stockItemId;
        row.itemName = batch.stockItemName;
        row.storeId = batch.storeId;
        row.storeName = batch.storeName;
        row.batchNumber = batch.batchNumber;
        row.currentStockAtHand = nz(batch.stockAtHand);
        if (draft != null) {
            row.draftQuantity = draft.draftQuantity;
            row.draftId = draft.id;
        }
        return row;
    }

    @Transactional
    public Response saveDraft(StockInitializerRequest request) {
        if (request == null || request.quantity == null || request.quantity.compareTo(BigDecimal.ZERO) < 0) {
            return badRequest("A valid quantity is required.");
        }
        boolean useStockBatch = resolveUseStockBatch();
        ResolvedTarget target = resolveTarget(request, useStockBatch, false);
        if (target.error != null) {
            return badRequest(target.error);
        }
        if (isFinalized(target, useStockBatch)) {
            return badRequest("This item has already been initialized and cannot be updated here.");
        }

        LocalDateTime now = LocalDateTime.now();
        StockInitialization record = findDraft(target, useStockBatch);
        if (record == null) {
            record = new StockInitialization();
            record.createdAt = now;
            applyTarget(record, target, useStockBatch);
        }
        record.draftQuantity = request.quantity;
        record.finalized = false;
        record.performedByUserId = request.performedByUserId;
        record.performedByUserName = request.performedByUserName;
        record.updatedAt = now;
        stockInitializationRepository.persist(record);
        return Response.ok(new ResponseMessage("Draft saved", StockInitializationDTO.fromRecord(record))).build();
    }

    @Transactional
    public Response finalize(StockInitializerRequest request) {
        if (request == null || request.quantity == null || request.quantity.compareTo(BigDecimal.ZERO) < 0) {
            return badRequest("A valid initial stock quantity is required.");
        }
        boolean useStockBatch = resolveUseStockBatch();
        ResolvedTarget target = resolveTarget(request, useStockBatch, true);
        if (target.error != null) {
            return badRequest(target.error);
        }
        if (isFinalized(target, useStockBatch)) {
            return badRequest("This item has already been counted and initialized.");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal qty = request.quantity;

        if (useStockBatch) {
            StockBatch batch = resolveOrCreateBatch(target, qty);
            BigDecimal before = nz(batch.stockAtHand);
            batch.stockAtHand = qty;
            batch.upDateDateAndTime = now;
            stockBatchRepository.persist(batch);
            target.stockBatchId = batch.id;
            target.itemName = batch.stockItemName;
            target.storeId = batch.storeId;
            target.storeName = batch.storeName;
            stockTrackingService.recordBatchMovement(
                    batch,
                    before,
                    qty,
                    StockTrackingService.TX_IN,
                    qty.subtract(before).abs(),
                    StockTrackingService.SRC_STOCK_INITIALIZER,
                    null,
                    REF_STOCK_INITIALIZER
            );
        } else {
            Item item = itemRepository.findById(target.itemId);
            if (item == null) {
                return notFound("Shop item not found.");
            }
            item.stockAtHand = qty;
            itemRepository.persist(item);
            target.itemName = item.title;
        }

        StockInitialization record = findDraft(target, useStockBatch);
        if (record == null) {
            record = new StockInitialization();
            record.createdAt = now;
            applyTarget(record, target, useStockBatch);
        }
        record.draftQuantity = qty;
        record.initialQuantity = qty;
        record.finalized = true;
        record.finalizedAt = now;
        record.performedByUserId = request.performedByUserId;
        record.performedByUserName = request.performedByUserName;
        record.updatedAt = now;
        stockInitializationRepository.persist(record);

        StockInitializationDTO dto = StockInitializationDTO.fromRecord(record);
        dto.currentStockAtHand = qty;
        return Response.ok(new ResponseMessage("Stock counted and initialized", dto)).build();
    }

    private StockBatch resolveOrCreateBatch(ResolvedTarget target, BigDecimal qty) {
        if (target.stockBatchId != null) {
            StockBatch existing = stockBatchRepository.findById(target.stockBatchId);
            if (existing != null) {
                return existing;
            }
        }
        Store store = storeRepository.findById(target.storeId);
        StockItem stockItem = stockItemRepository.findById(target.stockItemId);
        if (store == null || stockItem == null) {
            throw new IllegalStateException("Store and stock item are required to create an opening batch.");
        }

        StockBatch batch = stockBatchRepository.find(
                "storeId = ?1 and stockItemId = ?2",
                target.storeId,
                target.stockItemId
        ).firstResult();

        if (batch != null) {
            return batch;
        }

        batch = new StockBatch();
        batch.storeId = store.id;
        batch.storeName = store.name;
        batch.stockItemId = stockItem.id;
        batch.stockItemName = stockItem.stockItemName;
        batch.stockAtHand = qty;
        batch.unitCostPrice = BigDecimal.ZERO;
        batch.unitSellingPrice = BigDecimal.ZERO;
        batch.batchNumber = "OPENING";
        batch.expiryDate = LocalDate.now().plusYears(5);
        batch.creationDateAndTime = LocalDateTime.now();
        batch.lastUnitOfMeasure = stockItem.lastUnitOfSellMeasure;
        batch.lastUnitValue = stockItem.lastUnitOfSellMeasureStrength;

        Integer highestShelfNumber = stockBatchRepository.find("storeId = ?1 order by shelfNumber desc", store.id)
                .firstResultOptional()
                .map(b -> b.shelfNumber)
                .orElse(0);
        batch.shelfNumber = highestShelfNumber + 1;
        stockBatchRepository.persist(batch);
        return batch;
    }

    private StockInitialization findDraft(ResolvedTarget target, boolean useStockBatch) {
        String key = targetKey(target, useStockBatch);
        return stockInitializationRepository.listAll().stream()
                .filter(r -> !r.finalized && recordKey(r, useStockBatch).equals(key))
                .findFirst()
                .orElse(null);
    }

    private boolean isFinalized(ResolvedTarget target, boolean useStockBatch) {
        String key = targetKey(target, useStockBatch);
        return stockInitializationRepository.listAll().stream()
                .anyMatch(r -> r.finalized && recordKey(r, useStockBatch).equals(key));
    }

    private void applyTarget(StockInitialization record, ResolvedTarget target, boolean useStockBatch) {
        record.stockBatchId = target.stockBatchId;
        record.stockItemId = target.stockItemId;
        record.itemId = target.itemId;
        record.itemName = target.itemName;
        record.storeId = target.storeId;
        record.storeName = target.storeName;
    }

    private ResolvedTarget resolveTarget(StockInitializerRequest request, boolean useStockBatch, boolean strict) {
        ResolvedTarget target = new ResolvedTarget();
        if (useStockBatch) {
            if (request.stockBatchId != null) {
                StockBatch batch = stockBatchRepository.findById(request.stockBatchId);
                if (batch == null) {
                    target.error = "Stock batch not found.";
                    return target;
                }
                target.stockBatchId = batch.id;
                target.stockItemId = batch.stockItemId;
                target.storeId = batch.storeId;
                target.storeName = batch.storeName;
                target.itemName = batch.stockItemName;
                return target;
            }
            if (request.stockItemId == null) {
                target.error = "Select a stock batch or stock item.";
                return target;
            }
            StockItem stockItem = stockItemRepository.findById(request.stockItemId);
            if (stockItem == null) {
                target.error = "Stock item not found.";
                return target;
            }
            target.stockItemId = stockItem.id;
            target.itemName = stockItem.stockItemName;
            Long storeId = request.storeId;
            if (storeId == null && strict) {
                target.error = "Store is required for catalog stock items.";
                return target;
            }
            if (storeId != null) {
                Store store = storeRepository.findById(storeId);
                if (store == null) {
                    target.error = "Store not found.";
                    return target;
                }
                target.storeId = store.id;
                target.storeName = store.name;
            }
            return target;
        }

        if (request.itemId == null) {
            target.error = "Select a shop item.";
            return target;
        }
        Item item = itemRepository.findById(request.itemId);
        if (item == null) {
            target.error = "Shop item not found.";
            return target;
        }
        target.itemId = item.id;
        target.itemName = item.title;
        return target;
    }

    private void enrichCurrentStock(StockInitializationDTO dto, boolean useStockBatch) {
        if (useStockBatch && dto.stockBatchId != null) {
            StockBatch batch = stockBatchRepository.findById(dto.stockBatchId);
            if (batch != null) {
                dto.currentStockAtHand = nz(batch.stockAtHand);
            }
        } else if (!useStockBatch && dto.itemId != null) {
            Item item = itemRepository.findById(dto.itemId);
            if (item != null) {
                dto.currentStockAtHand = nz(item.stockAtHand);
            }
        }
    }

    private static String recordKey(StockInitialization record, boolean useStockBatch) {
        if (useStockBatch) {
            if (record.stockBatchId != null) {
                return batchKey(record.stockBatchId, record.stockItemId, record.storeId);
            }
            return catalogKey(record.stockItemId, record.storeId);
        }
        return shopKey(record.itemId);
    }

    private static String targetKey(ResolvedTarget target, boolean useStockBatch) {
        if (useStockBatch) {
            if (target.stockBatchId != null) {
                return batchKey(target.stockBatchId, target.stockItemId, target.storeId);
            }
            return catalogKey(target.stockItemId, target.storeId);
        }
        return shopKey(target.itemId);
    }

    private static String batchKey(Long stockBatchId, Long stockItemId, Long storeId) {
        return "batch:" + stockBatchId;
    }

    private static String catalogKey(Long stockItemId, Long storeId) {
        return "catalog:" + stockItemId + ":" + storeId;
    }

    private static String shopKey(Long itemId) {
        return "shop:" + itemId;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage(message, null)).build();
    }

    private static Response notFound(String message) {
        return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage(message, null)).build();
    }

    private static class ResolvedTarget {
        Long stockBatchId;
        Long stockItemId;
        Long itemId;
        Long storeId;
        String storeName;
        String itemName;
        String error;
    }
}
