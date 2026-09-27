package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;


import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class StockItem extends PanacheEntity {

    @Column
    public String stockItemName;

    @Column
    public Long brandId;

    @Column
    public String brandName;

    @Column
    public Long formulationId;

    @Column
    public String formulationName;

    @Column
    public Long lowestPackageId;

    @Column
    public String lowestPackageTitle;

    @Column
    public BigDecimal totalCompositionValue;

    @Column
    public Long totalCompositionUnitId;

    @Column
    public String totalCompositionUnitTitle;

    @Column
    public BigDecimal reconstitutionValue;

    @Column
    public Long reconstitutionUnitId;

    @Column
    public String reconstitutionUnitTitle;

    @Column
    public Long itemCategoryId;

    @Column
    public String itemCategoryName;

    @Column
    public Long routeOfAdminId;

    @Column
    public String routeOfAdminTitle;



        // updated field by the stock batch service

    @Column
    public String lastUnitOfSellMeasure;

    @Column
    public BigDecimal lastUnitOfSellMeasureStrength;

    @Column
    public Long lastUnitOfSellMeasureStrengthUnit;

    @Column
    public String lastUnitOfSellMeasureStrengthUnitTitle;

    //prescribing units

    @Column
    public Long lastUnitOfPrescribingMeasureStrengthUnit;

    @Column
    public String lastUnitOfPrescribingMeasureStrengthUnitTitle;

    /** Prescription period basis: DAY, WEEK, MONTH, BODY_WEIGHT */
    @Column
    public String prescribingPeriodBasis;

    /** JSON dosage templates for body weight / neonate / pediatric / adult */
    @Column(columnDefinition = "TEXT")
    public String prescribingDosageJson;

    /** FIXED_INTERVAL or CUSTOM_HOURS */
    @Column
    public String prescribingFrequencyMode;

    /** OD, BD, TDS, QID, etc. when mode is FIXED_INTERVAL */
    @Column
    public String prescribingFrequencyFixed;

    /** Comma-separated hours e.g. 0,12,24 when mode is CUSTOM_HOURS */
    @Column
    public String prescribingFrequencyCustomHours;

    //others



    @Column(columnDefinition = "TEXT")
    public String indication;

    @Column(columnDefinition = "TEXT")
    public String image;

    @Column(columnDefinition = "TEXT")
    public String image2;

    @Column(columnDefinition = "TEXT")
    public String image3;

    /** Local folder URL for primary image (served from PC product-images folder). */
    @Column(columnDefinition = "TEXT")
    public String imageLocal;

    @Column(columnDefinition = "TEXT")
    public String imageLocal2;

    @Column(columnDefinition = "TEXT")
    public String imageLocal3;

    /** Accepted barcodes (comma-separated), separate from internal sku. */
    @Column(columnDefinition = "TEXT")
    public String barcode;

    /** Primary product code / SKU. */
    @Column
    public String sku;

    @Column
    public String shelfNumber;

    /** Business category label (e.g. Supermarket, Hardware). */
    @Column
    public String businessCategory;

    /** Sale category label (e.g. Retail, Wholesale, Service). */
    @Column
    public String saleCategory;

    @Column
    public String unit;

    @Column
    public String unitShort;

    @Column
    public BigDecimal costPrice;

    @Column
    public BigDecimal sellingPrice;

    /**
     * When true (default), selling price is fixed via unit selling models.
     * When false, price is open and may be set at point of sale.
     */
    @Column
    public Boolean useUnitSellingModel;

    @Column
    public BigDecimal stockQty;

    @Column
    public BigDecimal reorderLevel;

    @Column
    public BigDecimal restockTo;

    @Column(columnDefinition = "TEXT")
    public String contraIndication;

    @Column(columnDefinition = "TEXT")
    public String drugIteractions;
    
    @Column(columnDefinition = "TEXT")
    public String description;
    
    @Column(columnDefinition = "TEXT")
    public String pharmacodynamics;
    
    @Column(columnDefinition = "TEXT")
    public String pharmacokinetics;

    @Column(columnDefinition = "TEXT")
    public String adverseEffects;

    @Column(columnDefinition = "TEXT")
    public String dosage;

    @Column(columnDefinition = "TEXT")
    public String notes;
   
    @Column(columnDefinition = "TEXT")
    public String methodOfReconstitution;

    @Column(columnDefinition = "TEXT")
    public String descriptionBeforeReconstitution;

    @Column(columnDefinition = "TEXT")
    public String descriptionAfterReconstitution;

    @Column(columnDefinition = "TEXT")
    public String storage;

    @Column(columnDefinition = "TEXT")
    public String antiDote;


    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;
}
