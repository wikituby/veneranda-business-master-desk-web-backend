package org.example.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import io.vertx.mutiny.mysqlclient.MySQLPool;
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
import org.example.inventory.stock.services.payloads.requests.StockBatchRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockReceiveDTO;
import org.example.inventory.store.domains.Store;
import org.example.inventory.store.domains.repositories.StoreRepository;

import java.math.BigDecimal;
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
    MySQLPool client;

    @Inject
    EntityManager entityManager;

    public static final String INVALID_REQUEST = "Invalid request data!";

    @Transactional
    public Response receiveStock(StockBatchRequest request) {

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

        StockSupplier stockSupplier = stockSupplierRepository.findById(request.stockSupplierId);
        if (stockSupplier == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock supplier not found", null))
                    .build();
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

            stockBatch.persist();
        } else {
            oldStockAtHand = stockBatch.stockAtHand;

            if (request.unitCostPrice.compareTo(stockBatch.unitCostPrice) > 0) {
                BigDecimal diff = request.unitCostPrice.subtract(stockBatch.unitCostPrice);
                stockBatch.unitSellingPrice = stockBatch.unitSellingPrice.add(diff);
                stockBatch.unitCostPrice = request.unitCostPrice;
            }

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
    }}
