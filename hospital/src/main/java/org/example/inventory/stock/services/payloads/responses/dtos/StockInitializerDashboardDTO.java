package org.example.inventory.stock.services.payloads.responses.dtos;

import java.util.ArrayList;
import java.util.List;

public class StockInitializerDashboardDTO {
    public boolean useStockBatch = true;
    public List<StockInitializerPendingDTO> pending = new ArrayList<>();
    public List<StockInitializationDTO> initialized = new ArrayList<>();
    public int pendingCount;
    public int initializedCount;

    public static class StockInitializerPendingDTO {
        public Long stockBatchId;
        public Long stockItemId;
        public Long itemId;
        public String itemName;
        public Long storeId;
        public String storeName;
        public String batchNumber;
        public java.math.BigDecimal currentStockAtHand;
        public java.math.BigDecimal draftQuantity;
        public Long draftId;
    }
}
