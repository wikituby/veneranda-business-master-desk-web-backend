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

    // Others - medical information fields
    public String indication;
    public String image;
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

    /** Active-ingredient rows; element type is StockItemIngredientRequest. */
    public List<StockItemIngredientRequest> ingredients;
}
