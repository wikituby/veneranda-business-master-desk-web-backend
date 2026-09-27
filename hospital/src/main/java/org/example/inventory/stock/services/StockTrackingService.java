package org.example.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockTracking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class StockTrackingService {

    public static final String TX_IN = "IN";
    public static final String TX_OUT = "OUT";

    public static final String SRC_STOCK_RECEIVE = "STOCK_RECEIVE";
    public static final String SRC_STOCK_RECEIVE_DELETE = "STOCK_RECEIVE_DELETE";
    public static final String SRC_STOCK_TRANSFER_OUT = "STOCK_TRANSFER_OUT";
    public static final String SRC_STOCK_TRANSFER_IN = "STOCK_TRANSFER_IN";
    public static final String SRC_TREATMENT_CHART_DISPENSE = "TREATMENT_CHART_DISPENSE";
    public static final String SRC_TREATMENT_CHART_DELETE = "TREATMENT_CHART_DELETE";
    public static final String SRC_TREATMENT_CHART_ADJUST = "TREATMENT_CHART_ADJUST";
    public static final String SRC_EXPIRY_ITEM_REGISTER = "EXPIRY_ITEM_REGISTER";
    public static final String SRC_STOCK_ADJUSTMENT = "STOCK_ADJUSTMENT";
    public static final String SRC_STOCK_INITIALIZER = "STOCK_INITIALIZER";
    public static final String SRC_OTC_PHARMACY_SALE = "OTC_PHARMACY_SALE";
    public static final String SRC_OTC_PHARMACY_SALE_VOID = "OTC_PHARMACY_SALE_VOID";
    public static final String SRC_VISIT_SUNDRY_DISPENSE = "VISIT_SUNDRY_DISPENSE";
    public static final String SRC_VISIT_SUNDRY_DELETE = "VISIT_SUNDRY_DELETE";

    public static final String REF_STOCK_RECEIVE = "StockReceive";
    public static final String REF_STOCK_TRANSFER = "StockTransfer";
    public static final String REF_TREATMENT_CHART = "TreatmentChart";
    public static final String REF_EXPIRY_ITEM_REGISTER = "ExpiryItemRegister";
    public static final String REF_STOCK_ADJUSTMENT = "StockAdjustment";
    public static final String REF_STOCK_INITIALIZER = "StockInitialization";
    public static final String REF_OTC_PHARMACY_SALE = "OtcPharmacySale";
    public static final String REF_VISIT_SUNDRY = "VisitSundry";

    /**
     * Persist one audit row for a batch-level quantity change.
     *
     * @param transactionType {@link #TX_IN} or {@link #TX_OUT}
     * @param quantityChanged positive magnitude of units moved
     */
    @Transactional
    public StockTracking recordBatchMovement(
            StockBatch batch,
            BigDecimal stockBefore,
            BigDecimal stockAfter,
            String transactionType,
            BigDecimal quantityChanged,
            String sourceEvent,
            Long referenceId,
            String referenceType) {
        if (batch == null || batch.stockItemId == null) {
            return null;
        }
        StockTracking st = new StockTracking();
        st.recordedAt = LocalDateTime.now();
        st.stockItemId = batch.stockItemId;
        st.stockBatchId = batch.id;
        st.storeId = batch.storeId;
        st.stockBeforeTransaction = stockBefore;
        st.stockAfterTransaction = stockAfter;
        st.transactionType = transactionType;
        st.quantityChanged = quantityChanged != null ? quantityChanged : BigDecimal.ZERO;
        st.sourceEvent = sourceEvent;
        st.referenceId = referenceId;
        st.referenceType = referenceType;
        st.persist();
        return st;
    }

    /**
     * Create and save a new StockTracking record (manual / legacy API).
     */
    @Transactional
    public StockTracking createStockTracking(Long stockItemId, BigDecimal stockBeforeTransaction,
                                             String transactionType, BigDecimal quantityChanged,
                                             BigDecimal stockAfterTransaction) {
        StockTracking stockTracking = new StockTracking();
        stockTracking.recordedAt = LocalDateTime.now();
        stockTracking.stockItemId = stockItemId;
        stockTracking.stockBeforeTransaction = stockBeforeTransaction;
        stockTracking.transactionType = transactionType;
        stockTracking.quantityChanged = quantityChanged;
        stockTracking.stockAfterTransaction = stockAfterTransaction;
        stockTracking.persist();
        return stockTracking;
    }

    public List<StockTracking> getAll() {
        return StockTracking.listAll();
    }

    public StockTracking getById(Long id) {
        return StockTracking.findById(id);
    }

    public List<StockTracking> getByStockItem(Long stockItemId) {
        return StockTracking.list("stockItemId = ?1", Sort.ascending("recordedAt", "id"), stockItemId);
    }

    @Transactional
    public boolean delete(Long id) {
        return StockTracking.deleteById(id);
    }

    public BigDecimal getTotalStockIn(Long stockItemId) {
        List<StockTracking> records = StockTracking.list("stockItemId = ?1 and transactionType = ?2", stockItemId, TX_IN);
        return records.stream()
                .map(r -> r.quantityChanged != null ? r.quantityChanged : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalStockOut(Long stockItemId) {
        List<StockTracking> records = StockTracking.list("stockItemId = ?1 and transactionType = ?2", stockItemId, TX_OUT);
        return records.stream()
                .map(r -> r.quantityChanged != null ? r.quantityChanged : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
