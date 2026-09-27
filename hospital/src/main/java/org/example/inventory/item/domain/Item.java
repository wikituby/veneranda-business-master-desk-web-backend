package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
public class Item extends PanacheEntity {


    @Column
    public String number;

    @ManyToOne
    @JoinColumn(name = "category_id")
    public ItemCategory category;

    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    public ItemCategory parentCategory;

    @Column
    public String subSubSubCategory;

    @Column
    public String indication;

    @Column
    public String contraIndication;

    @Column
    public String subCategory;

    @Column
    public String subSubCategory;

    @Column
    public String formulation; // e.g. "Tablet", "Syrup", "Injection"

    @Column
    public String activeIngredient;

    @Column
    public String strength;

    @Column
    public String manufacturer;

    @Column
    public String batchNumber;

    @Column(nullable = false)
    public String title;

    @Column
    public String genericName;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    public BigDecimal costPrice;

    @Column
    public BigDecimal sellingPrice;

    @Column
    public Long unitSellingModelId;

    @Column
    public String image;

    @Column
    public BigDecimal stockAtHand;

    @Column
    public Integer reOrderLevel;

    @Column
    public Integer reOrderTo;

    @Column
    public Integer shelfNumber;

    @Column
    public String unitOfMeasure;


    @Column
    public String lastUnitOfMeasure;

    @Column
    public BigDecimal lastUnitValue;

    // -----------------------------
    // Medication-like fields (optional)
    // -----------------------------

    @Column
    public BigDecimal dosage;

    @Column
    public String dosageUnit;

    @Column
    public BigDecimal frequency;

    @Column
    public String frequencyUnit;

    @Column
    public BigDecimal duration;

    @Column
    public String durationUnit;

    @Column
    public String route;


    @Column
    public String brand;

    @Column
    public String packaging;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;


}




