package org.example.inventory.stock.services.payloads.responses.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StockVerificationDashboardDTO {
    /** From facility business settings — true = stock batches, false = shop items. */
    public boolean useStockBatch = true;
    public List<StockVerificationDashboardRowDTO> rows = new ArrayList<>();

    public static class StockVerificationDashboardRowDTO {
        public Long stockBatchId;
        public Long itemId;
        public Long stockItemId;
        public String stockItemName;
        public String storeName;
        public String batchNumber;
        public BigDecimal stockAtHand;
        public StockVerificationDTO lastVerification;
        public StockVerificationPreviewDTO currentPreview;
    }
}
