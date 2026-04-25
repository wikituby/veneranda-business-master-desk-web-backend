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

    //others



    @Column(columnDefinition = "TEXT")
    public String indication;

    @Column(columnDefinition = "TEXT")
    public String image;

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
