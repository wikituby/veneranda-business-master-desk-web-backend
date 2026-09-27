package org.example.inventory.item.services.payloads.responses;

import java.math.BigDecimal;
import org.example.inventory.item.domain.Item;

public class ItemDTO {
    public Long id;
    public String number;
    public Long categoryId;
    public String categoryName;
    public Long parentCategoryId;
    public String parentCategoryName;
    public String subCategory;
    public String title;
    public Integer shelfNumber;
    public String description;
    public BigDecimal costPrice;
    public BigDecimal sellingPrice;
    public Long unitSellingModelId;
    public String unitOfMeasure;
    public String lastUnitOfMeasure;
    public BigDecimal lastUnitValue;
    public Integer reOrderLevel;
    public BigDecimal stockAtHand;
    public Integer reOrderTo;
    public BigDecimal dosage;
    public String dosageUnit;
    public BigDecimal frequency;
    public String frequencyUnit;
    public BigDecimal duration;
    public String durationUnit;
    public String route;

    public ItemDTO(Item item) {
        this.id = item.id;
        this.reOrderTo = item.reOrderTo;
        this.shelfNumber = item.shelfNumber;
        this.stockAtHand = item.stockAtHand;
        this.number = item.number;
        this.categoryId = item.category != null ? item.category.id : null;
        this.categoryName = item.category != null ? item.category.name : null;
        this.parentCategoryId = item.parentCategory != null ? item.parentCategory.id : null;
        this.parentCategoryName = item.parentCategory != null ? item.parentCategory.name : null;
        this.subCategory = item.subCategory;
        this.title = item.title;
        this.description = item.description;
        this.costPrice = item.costPrice;
        this.sellingPrice = item.sellingPrice;
        this.unitSellingModelId = item.unitSellingModelId;
        this.unitOfMeasure = item.unitOfMeasure;
        this.lastUnitOfMeasure = item.lastUnitOfMeasure;
        this.lastUnitValue = item.lastUnitValue;
        this.dosage = item.dosage;
        this.dosageUnit = item.dosageUnit;
        this.frequency = item.frequency;
        this.frequencyUnit = item.frequencyUnit;
        this.duration = item.duration;
        this.durationUnit = item.durationUnit;
        this.route = item.route;
        this.reOrderLevel = item.reOrderLevel;
    }
}
