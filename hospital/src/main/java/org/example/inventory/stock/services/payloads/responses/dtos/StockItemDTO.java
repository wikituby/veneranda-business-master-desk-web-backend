package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.StockItem;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockItemDTO {
    public Long id;
    public String stockItemName;
    public String brandName;
    public Long brandId;
    public Long formulationId;
    public String formulationName;
    public Long lowestPackageId;
    public String lowestPackageTitle;
    public BigDecimal totalCompositionValue;
    public Long totalCompositionUnitId;
    public String totalCompositionUnitTitle;
    public BigDecimal reconstitutionValue;
    public Long reconstitutionUnitId;
    public String reconstitutionUnitTitle;
    public Long itemCategoryId;
    public String itemCategoryName;
    /** Top-most parent category (e.g. Drug when leaf is Antibiotic). */
    public Long lastCategoryId;
    public String lastCategoryName;
    public Long routeOfAdminId;
    public String routeOfAdminTitle;
    
    // Updated fields by the stock batch service
    public String lastUnitOfSellMeasure;
    public BigDecimal lastUnitOfSellMeasureStrength;
    public Long lastUnitOfSellMeasureStrengthUnit;
    public String lastUnitOfSellMeasureStrengthUnitTitle;
    
    // Prescribing units
    public Long lastUnitOfPrescribingMeasureStrengthUnit;
    public String lastUnitOfPrescribingMeasureStrengthUnitTitle;
    public String prescribingPeriodBasis;
    public String prescribingDosageJson;
    public String prescribingFrequencyMode;
    public String prescribingFrequencyFixed;
    public String prescribingFrequencyCustomHours;
    
    // Others - medical information fields
    public String indication;
    public java.util.List<StockItemIndicationDTO> indications;
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
    
    public LocalDate creationDate;
    public LocalDate updateDate;

    public StockItemDTO(StockItem entity) {
        this.id = entity.id;
        this.stockItemName = entity.stockItemName;
        this.brandId = entity.brandId;
        this.brandName = entity.brandName;
        this.formulationId = entity.formulationId;
        this.formulationName = entity.formulationName;
        this.lowestPackageId = entity.lowestPackageId;
        this.lowestPackageTitle = entity.lowestPackageTitle;
        this.totalCompositionValue = entity.totalCompositionValue;
        this.totalCompositionUnitId = entity.totalCompositionUnitId;
        this.totalCompositionUnitTitle = entity.totalCompositionUnitTitle;
        this.reconstitutionValue = entity.reconstitutionValue;
        this.reconstitutionUnitId = entity.reconstitutionUnitId;
        this.reconstitutionUnitTitle = entity.reconstitutionUnitTitle;
        this.itemCategoryId = entity.itemCategoryId;
        this.itemCategoryName = entity.itemCategoryName;
        this.routeOfAdminId = entity.routeOfAdminId;
        this.routeOfAdminTitle = entity.routeOfAdminTitle;
        
        // Updated fields by the stock batch service
        this.lastUnitOfSellMeasure = entity.lastUnitOfSellMeasure;
        this.lastUnitOfSellMeasureStrength = entity.lastUnitOfSellMeasureStrength;
        this.lastUnitOfSellMeasureStrengthUnit = entity.lastUnitOfSellMeasureStrengthUnit;
        this.lastUnitOfSellMeasureStrengthUnitTitle = entity.lastUnitOfSellMeasureStrengthUnitTitle;
        
        // Prescribing units
        this.lastUnitOfPrescribingMeasureStrengthUnit = entity.lastUnitOfPrescribingMeasureStrengthUnit;
        this.lastUnitOfPrescribingMeasureStrengthUnitTitle = entity.lastUnitOfPrescribingMeasureStrengthUnitTitle;
        this.prescribingPeriodBasis = entity.prescribingPeriodBasis;
        this.prescribingDosageJson = entity.prescribingDosageJson;
        this.prescribingFrequencyMode = entity.prescribingFrequencyMode;
        this.prescribingFrequencyFixed = entity.prescribingFrequencyFixed;
        this.prescribingFrequencyCustomHours = entity.prescribingFrequencyCustomHours;
        
        // Others - medical information fields
        this.indication = entity.indication;
        this.image = entity.image;
        this.image2 = entity.image2;
        this.image3 = entity.image3;
        this.imageLocal = entity.imageLocal;
        this.imageLocal2 = entity.imageLocal2;
        this.imageLocal3 = entity.imageLocal3;
        this.barcode = entity.barcode;
        this.sku = entity.sku;
        this.shelfNumber = entity.shelfNumber;
        this.businessCategory = entity.businessCategory;
        this.saleCategory = entity.saleCategory;
        this.unit = entity.unit;
        this.unitShort = entity.unitShort;
        this.costPrice = entity.costPrice;
        this.sellingPrice = entity.sellingPrice;
        this.useUnitSellingModel = entity.useUnitSellingModel == null || Boolean.TRUE.equals(entity.useUnitSellingModel);
        this.stockQty = entity.stockQty;
        this.reorderLevel = entity.reorderLevel;
        this.restockTo = entity.restockTo;
        this.contraIndication = entity.contraIndication;
        this.drugIteractions = entity.drugIteractions;
        this.description = entity.description;
        this.pharmacodynamics = entity.pharmacodynamics;
        this.pharmacokinetics = entity.pharmacokinetics;
        this.adverseEffects = entity.adverseEffects;
        this.dosage = entity.dosage;
        this.notes = entity.notes;
        this.methodOfReconstitution = entity.methodOfReconstitution;
        this.descriptionBeforeReconstitution = entity.descriptionBeforeReconstitution;
        this.descriptionAfterReconstitution = entity.descriptionAfterReconstitution;
        this.storage = entity.storage;
        this.antiDote = entity.antiDote;
        
        this.creationDate = entity.creationDate;
        this.updateDate = entity.updateDate;
    }
}
