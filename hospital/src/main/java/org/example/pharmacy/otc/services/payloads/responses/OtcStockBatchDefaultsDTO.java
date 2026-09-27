package org.example.pharmacy.otc.services.payloads.responses;

import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockItem;

import java.math.BigDecimal;

/** Prefill values for the OTC sale line form when a stock batch is selected. */
public class OtcStockBatchDefaultsDTO {
    public Long stockBatchId;
    public String itemName;
    public BigDecimal amountPerFrequencyValue;
    public String amountPerFrequencyUnit;
    /** Alias for amountPerFrequencyUnit (dosage unit field). */
    public String dosageUnit;
    public String route;
    /** Combined label for display, e.g. 200mg */
    public String dosage;

    public OtcStockBatchDefaultsDTO(StockBatch batch) {
        if (batch == null) {
            return;
        }
        this.stockBatchId = batch.id;
        this.itemName = batch.stockItemName;
        if (batch.stockItemId == null) {
            return;
        }
        StockItem item = StockItem.findById(batch.stockItemId);
        if (item == null) {
            return;
        }
        String unit = item.lastUnitOfPrescribingMeasureStrengthUnitTitle;
        this.amountPerFrequencyValue = item.lastUnitOfSellMeasureStrength;
        this.amountPerFrequencyUnit = unit;
        this.dosageUnit = unit;
        this.route = item.routeOfAdminTitle;
        this.dosage = formatDosage(this.amountPerFrequencyValue, unit);
    }

    private static String formatDosage(BigDecimal value, String unit) {
        if (value == null && (unit == null || unit.isBlank())) {
            return null;
        }
        if (value == null) {
            return unit.trim();
        }
        String amount = value.stripTrailingZeros().toPlainString();
        if (unit == null || unit.isBlank()) {
            return amount;
        }
        return amount + unit.trim();
    }
}
