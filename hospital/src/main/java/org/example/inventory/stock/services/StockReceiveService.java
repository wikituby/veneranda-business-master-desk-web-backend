package org.example.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.repositories.BrandRepository;
import org.example.inventory.item.services.ShopItemService;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockItem;
import org.example.inventory.stock.domains.StockReceive;
import org.example.inventory.stock.domains.StockSupplier;
import org.example.inventory.stock.domains.repositories.StockBatchRepository;
import org.example.inventory.stock.domains.repositories.StockItemRepository;
import org.example.inventory.stock.domains.repositories.StockReceiveRepository;
import org.example.inventory.stock.domains.repositories.StockSupplierRepository;
import org.example.inventory.stock.services.StockTrackingService;
import org.example.inventory.stock.services.UnitSellingModelService;
import org.example.inventory.stock.services.payloads.requests.StockBatchRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockReceiveDTO;
import org.example.inventory.store.domains.Store;
import org.example.inventory.store.domains.repositories.StoreRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class StockReceiveService {

    @Inject
    StockReceiveRepository stockReceiveRepository;

    @Inject
    StockBatchRepository stockBatchRepository;

    @Inject
    StockItemRepository stockItemRepository;

    @Inject
    StockSupplierRepository stockSupplierRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    BrandRepository brandRepository;

    @Inject
    ShopItemService itemService;

    @Inject
    StockTrackingService stockTrackingService;

    @Inject
    Pool client;

    @Inject
    EntityManager entityManager;

    @Inject
    UnitSellingModelService unitSellingModelService;

    public static final String INVALID_REQUEST = "Invalid request data!";

    @Transactional
    public Response receiveStock(StockBatchRequest request) {

        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(INVALID_REQUEST, null))
                    .build();
        }
        if (request.storeId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Store is required", null))
                    .build();
        }
        if (request.stockItemId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Stock item is required", null))
                    .build();
        }

        Store store = storeRepository.findById(request.storeId);
        if (store == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Store not found", null))
                    .build();
        }

        StockItem stockItem = stockItemRepository.findById(request.stockItemId);
        if (stockItem == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock item not found", null))
                    .build();
        }

        StockSupplier stockSupplier = resolveSupplier(request.stockSupplierId);
        if (stockSupplier == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Could not resolve a stock supplier for this receive", null))
                    .build();
        }
        request.stockSupplierId = stockSupplier.id;

        request.unitCostPrice = money(request.unitCostPrice);
        request.unitSellingPrice = money(request.unitSellingPrice);
        request.totalCostPrice = money(request.totalCostPrice);

        if (request.unitSellingModelId != null) {
            request.unitSellingPrice = unitSellingModelService.resolveUnitSellingPrice(
                    request.stockItemId,
                    request.unitSellingModelId,
                    request.unitSellingPrice
            );
        } else {
            unitSellingModelService.ensureModelsForStockItem(request.stockItemId, request.unitCostPrice);
        }

        StockBatch stockBatch = stockBatchRepository.find(
                "storeId = ?1 and stockItemId = ?2 and expiryDate = ?3",
                request.storeId, request.stockItemId, request.expiryDate
        ).firstResult();

        BigDecimal oldStockAtHand = BigDecimal.ZERO;

        if (stockBatch == null) {
            stockBatch = new StockBatch();
            stockBatch.storeId = store.id;
            stockBatch.storeName = store.name;
            stockBatch.stockItemId = stockItem.id;
            stockBatch.stockItemName = stockItem.stockItemName;
            stockBatch.expiryDate = request.expiryDate;
            stockBatch.batchNumber = request.batchNumber;
            stockBatch.unitCostPrice = request.unitCostPrice;
            stockBatch.unitSellingPrice = request.unitSellingPrice;
            stockBatch.unitSellingModelId = request.unitSellingModelId;
            stockBatch.stockAtHand = request.quantityReceived;
            stockBatch.stockSupplierName = stockSupplier.supplierName;
            stockBatch.stockSupplierId = stockSupplier.id;
            stockBatch.lastUnitOfMeasure = stockItem.lastUnitOfSellMeasure;
            stockBatch.lastUnitValue = stockItem.lastUnitOfSellMeasureStrength;

            Integer highestShelfNumber = (Integer) entityManager.createQuery(
                    "SELECT MAX(sb.shelfNumber) FROM StockBatch sb WHERE sb.storeId = :storeId"
            ).setParameter("storeId", request.storeId)
                    .getSingleResult();

            if (highestShelfNumber == null) {
                highestShelfNumber = 0;
            }

            stockBatch.shelfNumber = highestShelfNumber + 1;
            stockBatch.lastUnitOfMeasure = request.unitOfMeasure;
            stockBatch.packaging = request.packaging;
            stockBatch.creationDateAndTime = LocalDateTime.now();
            applyProfitMargins(stockBatch, request);

            stockBatch.persist();
        } else {
            oldStockAtHand = stockBatch.stockAtHand;

            stockBatch.unitCostPrice = request.unitCostPrice;
            if (request.unitSellingPrice != null
                    && request.unitSellingPrice.compareTo(BigDecimal.ZERO) > 0) {
                stockBatch.unitSellingPrice = request.unitSellingPrice;
            }
            if (request.unitSellingModelId != null) {
                stockBatch.unitSellingModelId = request.unitSellingModelId;
            }
            applyProfitMargins(stockBatch, request);

            stockBatch.stockAtHand = stockBatch.stockAtHand.add(request.quantityReceived);
            stockBatch.lastUnitOfMeasure = request.unitOfMeasure;
            stockBatch.packaging = request.packaging;

            stockBatch.persist();
        }

        StockReceive stockReceive = new StockReceive();
        stockReceive.stockBatchId = stockBatch.id;
        stockReceive.stockItemName = stockBatch.stockItemName;
        stockReceive.storeId = store.id;
        stockReceive.storeName = store.name;
        stockReceive.quantityReceived = request.quantityReceived;
        stockReceive.unitCostPrice = request.unitCostPrice;
        stockReceive.totalCostPrice = request.totalCostPrice;
        stockReceive.unitSellingPrice = stockBatch.unitSellingPrice;
        stockReceive.quantityAvailable = oldStockAtHand;
        stockReceive.newQuantity = stockBatch.stockAtHand;
        stockReceive.receiveDate = request.receiveDate;
        stockReceive.supplierName = stockSupplier.supplierName;
        stockReceive.supplierId = stockSupplier.id;
        stockReceive.invoiceNumber = request.invoiceNumber;
        stockReceive.batchNumber = request.batchNumber;
        stockReceive.purchaseOrderId = request.purchaseOrderId;
        stockReceive.purchaseOrderNo = request.purchaseOrderNo;
        stockReceive.packaging = request.packaging;
        stockReceive.expiryDate = request.expiryDate;
        stockReceive.persist();

        BigDecimal qtyBeforeReceive = oldStockAtHand;
        BigDecimal qtyAfterReceive = stockBatch.stockAtHand;
        stockTrackingService.recordBatchMovement(
                stockBatch,
                qtyBeforeReceive,
                qtyAfterReceive,
                StockTrackingService.TX_IN,
                request.quantityReceived,
                StockTrackingService.SRC_STOCK_RECEIVE,
                stockReceive.id,
                StockTrackingService.REF_STOCK_RECEIVE);

        return Response.ok(new ResponseMessage("Stock Received successfully", new StockReceiveDTO(stockReceive))).build();
    }

    @Transactional
    public List<StockReceive> getAllStockReceives() {
        return stockReceiveRepository.listAll(Sort.descending("id"));
    }

    @Transactional
    public Response deleteStockReceivedById(Long id) {
        StockReceive stockReceive = stockReceiveRepository.findById(id);
        if (stockReceive == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock receive record not found", null))
                    .build();
        }

        StockBatch batch = stockBatchRepository.findById(stockReceive.stockBatchId);
        if (batch == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock batch not found for this receive record.", null))
                    .build();
        }

        BigDecimal quantity = stockReceive.quantityReceived;
        if (batch.stockAtHand == null || batch.stockAtHand.compareTo(quantity) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "Part of this Stock has already been sold, so you have to sell the rest of the stock first ",
                            null))
                    .build();
        }

        Long receiveId = stockReceive.id;
        BigDecimal beforeHand = batch.stockAtHand;
        BigDecimal afterHand = beforeHand.subtract(quantity);

        batch.stockAtHand = afterHand;
        stockBatchRepository.persist(batch);

        stockTrackingService.recordBatchMovement(
                batch,
                beforeHand,
                afterHand,
                StockTrackingService.TX_OUT,
                quantity,
                StockTrackingService.SRC_STOCK_RECEIVE_DELETE,
                receiveId,
                StockTrackingService.REF_STOCK_RECEIVE);

        stockReceiveRepository.delete(stockReceive);

        return Response.ok(new ResponseMessage(
                "Stock Receive deleted successfully, and stock at hand updated in the store", null)).build();
    }

    private void applyProfitMargins(StockBatch stockBatch, StockBatchRequest request) {
        if (request.profitMarginForRetail != null) {
            stockBatch.profitMarginForRetail = money(request.profitMarginForRetail);
        } else if (request.unitSellingPrice != null && request.unitCostPrice != null) {
            stockBatch.profitMarginForRetail = money(
                    request.unitSellingPrice.subtract(request.unitCostPrice).max(BigDecimal.ZERO));
        }
        if (request.profitMarginForWholeSale != null) {
            stockBatch.profitMarginForWholeSale = money(request.profitMarginForWholeSale);
        }
        if (request.profitMarginForSpecialCase != null) {
            stockBatch.profitMarginForSpecialCase = money(request.profitMarginForSpecialCase);
        }
    }

    /**
     * POS / quick receive may omit supplier. Prefer the requested id, else an existing
     * Direct/POS supplier, else any supplier, else create "Direct purchase".
     */
    private StockSupplier resolveSupplier(Long stockSupplierId) {
        if (stockSupplierId != null) {
            StockSupplier named = stockSupplierRepository.findById(stockSupplierId);
            if (named != null) {
                return named;
            }
        }

        java.util.Set<String> preferredNames = java.util.Set.of(
                "direct purchase", "direct", "pos", "walk-in", "walk in");
        List<StockSupplier> all = stockSupplierRepository.listAll(Sort.ascending("id"));
        for (StockSupplier s : all) {
            String name = s.supplierName == null ? "" : s.supplierName.trim().toLowerCase();
            if (preferredNames.contains(name)) {
                return s;
            }
        }
        if (!all.isEmpty()) {
            return all.get(0);
        }

        StockSupplier created = new StockSupplier();
        created.supplierName = "Direct purchase";
        created.description = "Auto-created for POS / quick stock receive";
        created.creationDateTime = LocalDateTime.now();
        stockSupplierRepository.persist(created);
        return created;
    }

    /** Store money fields to 2 decimal places so 200 is not saved as 199.9 from float drift. */
    private static BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
