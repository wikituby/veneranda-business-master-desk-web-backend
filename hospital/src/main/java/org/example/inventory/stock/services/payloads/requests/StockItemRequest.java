package org.example.inventory.stock.services.payloads.requests;

import java.math.BigDecimal;
import java.util.List;

public class StockItemRequest {
    public String stockItemName;
    public Long brandId;
    public Long formulationId;
    public Long lowestPackageId;
    public BigDecimal totalCompositionValue;
    public Long totalCompositionUnitId;
    public BigDecimal reconstitutionValue;
    public Long reconstitutionUnitId;
    public Long itemCategoryId;
    public Long routeOfAdminId;

    // Updated fields by the stock batch service
    public String lastUnitOfSellMeasure;
    public BigDecimal lastUnitOfSellMeasureStrength;
    public Long lastUnitOfSellMeasureStrengthUnit;

    // Prescribing units
    public Long lastUnitOfPrescribingMeasureStrengthUnit;
    public String prescribingPeriodBasis;
    public String prescribingDosageJson;
    public String prescribingFrequencyMode;
    public String prescribingFrequencyFixed;
    public String prescribingFrequencyCustomHours;

    // Others - medical information fields
    public String indication;
    /** Structured indication lines (preferred). When present, replaces legacy single indication TEXT. */
    public List<StockItemIndicationRequest> indications;
    public String image;
    public String image2;
    public String image3;
    public String imageLocal;
    public String imageLocal2;
    public String imageLocal3;
    public String barcode;
    public String sku;
    public String shelfNumber;
    public String businessCategory;
    public String saleCategory;
    public String unit;
    public String unitShort;
    public BigDecimal costPrice;
    public BigDecimal sellingPrice;
    /** true = fixed via unit sell model; false = open price at sale. Null treated as true. */
    public Boolean useUnitSellingModel;
    public BigDecimal stockQty;
    public BigDecimal reorderLevel;
    public BigDecimal restockTo;
    public String contraIndication;
    public String drugIteractions;
    public String description;
    public String pharmacodynamics;
    public String pharmacokinetics;
    public String adverseEffects;
    public String dosage;
    public String notes;
    public String methodOfReconstitution;
    public String descriptionBeforeReconstitution;
    public String descriptionAfterReconstitution;
    public String storage;
    public String antiDote;

    /**
     * When true, never update an existing row by name — return conflict if the
     * exact stock item name already exists (POS "add product" flow).
     */
    public Boolean createOnly;

    /** Active-ingredient rows; element type is StockItemIngredientRequest. */
    public List<StockItemIngredientRequest> ingredients;
}
