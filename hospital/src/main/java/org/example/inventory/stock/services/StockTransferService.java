package org.example.inventory.stock.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockTransfer;
import org.example.inventory.stock.domains.repositories.StockBatchRepository;
import org.example.inventory.stock.domains.repositories.StockTransferRepository;
import org.example.inventory.stock.services.StockTrackingService;
import org.example.inventory.stock.services.payloads.requests.StockTransferRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockTransferDTO;
import org.example.inventory.store.domains.Store;
import org.example.inventory.store.domains.repositories.StoreRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class StockTransferService {

    @Inject
    StockBatchRepository stockBatchRepository;

    @Inject
    StockTransferRepository transferRepo;

    @Inject
    StoreRepository storeRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    StockTrackingService stockTrackingService;

    @Transactional
    public StockTransferDTO transferStock(StockTransferRequest request) {
    
        // 1️⃣ Fetch the source StockBatch by its ID
        StockBatch from = stockBatchRepository.findById(request.stockBatchId);
    
        if (from == null) {
            throw new IllegalStateException("Source stock batch not found with ID: " + request.stockBatchId);
        }
    
        if (!from.storeId.equals(request.fromStoreId)) {
            throw new IllegalStateException("Source batch does not belong to the specified source store.");
        }
    
        // 2️⃣ Ensure there is enough stock to transfer
        if (from.stockAtHand == null || from.stockAtHand.compareTo(request.qty) < 0) {
            throw new IllegalStateException("Insufficient stock in source store.");
        }
    
        // 3️⃣ Find matching batch in destination store:
        // same item, same batchNo, same expiry
        StockBatch to = stockBatchRepository.find(
                "storeId = ?1 and stockItemId = ?2 and batchNumber = ?3 and expiryDate = ?4",
                request.toStoreId, from.stockItemId, from.batchNumber, from.expiryDate
        ).firstResult();
    
        // 4️⃣ If destination batch does not exist => create new batch (mirror source + destination store)
        if (to == null) {
            to = new StockBatch();
            to.storeId = request.toStoreId;
            to.stockItemId = from.stockItemId;
            to.stockItemName = from.stockItemName;
            to.batchNumber = from.batchNumber;
            to.expiryDate = from.expiryDate;
            to.unitCostPrice = from.unitCostPrice;
            to.unitSellingPrice = from.unitSellingPrice;
            to.stockSupplierId = from.stockSupplierId;
            to.stockSupplierName = from.stockSupplierName;
            to.packaging = from.packaging;
            to.unitOfMeasure = from.unitOfMeasure;
            to.lastUnitValue = from.lastUnitValue;
            to.lastUnitOfMeasure = from.lastUnitOfMeasure;
            to.reOrderLevel = from.reOrderLevel;
            to.reOrderQuantity = from.reOrderQuantity;
            to.reOrderTo = from.reOrderTo;
            to.profitMarginForRetail = from.profitMarginForRetail;
            to.profitMarginForWholeSale = from.profitMarginForWholeSale;
            to.profitMarginForSpecialCase = from.profitMarginForSpecialCase;

            Store destStore = storeRepository.findById(request.toStoreId);
            if (destStore != null) {
                to.storeName = destStore.name;
            }

            Integer highestShelfNumber = (Integer) entityManager.createQuery(
                    "SELECT MAX(sb.shelfNumber) FROM StockBatch sb WHERE sb.storeId = :storeId"
            ).setParameter("storeId", request.toStoreId)
                    .getSingleResult();
            to.shelfNumber = highestShelfNumber == null ? 1 : highestShelfNumber + 1;

            to.stockAtHand = BigDecimal.ZERO;
            to.creationDateAndTime = LocalDateTime.now();
            to.persist();
        } else {
            // Ensure denormalized fields exist if batch was created earlier without them
            if (to.storeName == null || to.storeName.isBlank()) {
                Store destStore = storeRepository.findById(request.toStoreId);
                if (destStore != null) {
                    to.storeName = destStore.name;
                }
            }
            if (to.stockItemName == null || to.stockItemName.isBlank()) {
                to.stockItemName = from.stockItemName;
            }
        }

        // 5️⃣ Adjust stock (destination may have null stockAtHand from legacy rows)
        BigDecimal fromBefore = from.stockAtHand;
        BigDecimal destBefore = to.stockAtHand != null ? to.stockAtHand : BigDecimal.ZERO;

        from.stockAtHand = from.stockAtHand.subtract(request.qty);
        to.stockAtHand = destBefore.add(request.qty);
        to.upDateDateAndTime = LocalDateTime.now();
        from.upDateDateAndTime = LocalDateTime.now();

        from.persist();
        to.persist();
    
        // 6️⃣ Record the transfer
        StockTransfer transfer = new StockTransfer();
        transfer.fromStoreId = request.fromStoreId;
        transfer.toStoreId = request.toStoreId;
        transfer.stockBatchId = request.stockBatchId;  // store source batch ID
        transfer.quantity = request.qty;
        transfer.transferredBy = request.transferredBy;
        transfer.transferDate = LocalDateTime.now();
    
        transferRepo.persist(transfer);

        stockTrackingService.recordBatchMovement(
                from,
                fromBefore,
                from.stockAtHand,
                StockTrackingService.TX_OUT,
                request.qty,
                StockTrackingService.SRC_STOCK_TRANSFER_OUT,
                transfer.id,
                StockTrackingService.REF_STOCK_TRANSFER);
        stockTrackingService.recordBatchMovement(
                to,
                destBefore,
                to.stockAtHand,
                StockTrackingService.TX_IN,
                request.qty,
                StockTrackingService.SRC_STOCK_TRANSFER_IN,
                transfer.id,
                StockTrackingService.REF_STOCK_TRANSFER);
    
        return new StockTransferDTO(transfer);
    }
    
    public BigDecimal getTotalItemStock(Long itemId) {
        return stockBatchRepository.find("stockItemId", itemId).stream()
                .map(stock -> stock.stockAtHand != null ? stock.stockAtHand : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<StockTransferDTO> findAllTransfers() {
        return transferRepo.listAll().stream()
                .sorted(Comparator.comparing((StockTransfer t) -> t.transferDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(StockTransferDTO::new)
                .collect(Collectors.toList());
    }





}
