package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.item.domain.StrengthUnit;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockItem;
import org.example.inventory.stock.services.payloads.responses.dtos.UnitSellingModelDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockBatchDTO {

    public Long id;
    public String stockItemName;
    public Long storeId;
    public String storeName;
    public Long stockItemId;
    public BigDecimal unitCostPrice;
    public BigDecimal unitSellingPrice;
    public BigDecimal stockAtHand;
    public Integer shelfNumber;
    public BigDecimal profitMarginForRetail;
    public BigDecimal profitMarginForWholeSale;
    public BigDecimal profitMarginForSpecialCase;
    public Integer reOrderLevel;
    public Integer reOrderTo;
    public Integer reOrderQuantity;
    public String unitOfMeasure;
    public BigDecimal lastUnitValue;
    public String lastUnitOfMeasure;
    public String batchNumber;
    public Long stockSupplierId;
    public String stockSupplierName;
    public String packaging;
    public LocalDateTime creationDateAndTime;
    public LocalDateTime upDateDateAndTime;
    public LocalDate expiryDate;
    /** Combined dose label from stock item, e.g. 200mg */
    public String dosage;
    public BigDecimal amountPerFrequencyValue;
    public String amountPerFrequencyUnit;
    /** Alias for amountPerFrequencyUnit (OTC form dosage unit field). */
    public String dosageUnit;
    public String route;
    public String routeOfAdminTitle;
    public String lastUnitOfPrescribingMeasureStrengthUnitTitle;
    public Long unitSellingModelId;
    public List<UnitSellingModelDTO> unitSellingModels = new ArrayList<>();
    /** Top-level inventory category (e.g. Drug, Sundries). */
    public Long lastCategoryId;
    public String lastCategoryName;
    /** Product images from StockItem (primary + optional extras). */
    public String image;
    public String image2;
    public String image3;
    public String imageLocal;
    public String imageLocal2;
    public String imageLocal3;

    public StockBatchDTO(StockBatch entity) {
        this.id = entity.id;
        this.reOrderTo = entity.reOrderTo;
        this.stockItemName = entity.stockItemName;
        this.storeId = entity.storeId;
        this.storeName = entity.storeName;
        this.stockItemId = entity.stockItemId;
        this.unitCostPrice = entity.unitCostPrice;
        this.unitSellingPrice = entity.unitSellingPrice;
        this.stockAtHand = entity.stockAtHand;
        this.shelfNumber = entity.shelfNumber;
        this.profitMarginForRetail = entity.profitMarginForRetail;
        this.profitMarginForWholeSale = entity.profitMarginForWholeSale;
        this.profitMarginForSpecialCase = entity.profitMarginForSpecialCase;
        this.reOrderLevel = entity.reOrderLevel;
        this.reOrderQuantity = entity.reOrderQuantity;
        this.unitOfMeasure = entity.unitOfMeasure;
        this.lastUnitValue = entity.lastUnitValue;
        this.lastUnitOfMeasure = entity.lastUnitOfMeasure;
        this.batchNumber = entity.batchNumber;
        this.stockSupplierId = entity.stockSupplierId;
        this.stockSupplierName = entity.stockSupplierName;
        this.packaging = entity.packaging;
        this.creationDateAndTime = entity.creationDateAndTime;
        this.upDateDateAndTime = entity.upDateDateAndTime;
        this.expiryDate = entity.expiryDate;
        this.unitSellingModelId = entity.unitSellingModelId;
        enrichPrescribingFromStockItem(entity.stockItemId);
        enrichImagesFromStockItem(entity.stockItemId);
    }

    private void enrichImagesFromStockItem(Long stockItemId) {
        if (stockItemId == null) {
            return;
        }
        StockItem item = StockItem.findById(stockItemId);
        if (item == null) {
            return;
        }
        this.image = item.image;
        this.image2 = item.image2;
        this.image3 = item.image3;
        this.imageLocal = item.imageLocal;
        this.imageLocal2 = item.imageLocal2;
        this.imageLocal3 = item.imageLocal3;
    }

    private void enrichPrescribingFromStockItem(Long stockItemId) {
        if (stockItemId == null) {
            return;
        }
        StockItem item = StockItem.findById(stockItemId);
        if (item == null) {
            return;
        }
        String unit = resolvePrescribingUnitLabel(item);
        this.lastUnitOfPrescribingMeasureStrengthUnitTitle = unit;
        this.amountPerFrequencyValue = item.totalCompositionValue != null
                ? item.totalCompositionValue
                : item.lastUnitOfSellMeasureStrength;
        this.amountPerFrequencyUnit = unit;
        this.dosageUnit = unit;
        this.route = item.routeOfAdminTitle;
        this.routeOfAdminTitle = item.routeOfAdminTitle;
        this.dosage = formatDosage(this.amountPerFrequencyValue, unit);
    }

    private static String resolvePrescribingUnitLabel(StockItem item) {
        if (item.lastUnitOfPrescribingMeasureStrengthUnit != null) {
            StrengthUnit strengthUnit = StrengthUnit.findById(item.lastUnitOfPrescribingMeasureStrengthUnit);
            if (strengthUnit != null) {
                String label = strengthUnit.prescribingLabel();
                if (label != null && !label.isBlank()) {
                    return label;
                }
            }
        }
        String stored = item.lastUnitOfPrescribingMeasureStrengthUnitTitle;
        return stored != null ? stored.trim() : null;
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
