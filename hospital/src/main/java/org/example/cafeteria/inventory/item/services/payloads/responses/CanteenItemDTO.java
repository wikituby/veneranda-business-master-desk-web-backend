package org.example.cafeteria.inventory.item.services.payloads.responses;

import org.example.cafeteria.inventory.item.domains.CanteenItem;

import java.math.BigDecimal;

public class CanteenItemDTO {
    public Long id;
    public String number;
    public String category;
    public String subCategory;
    public String title;
    public String description;
    public BigDecimal costPrice;
    public BigDecimal sellingPrice;
    public String unitOfMeasure;
    public BigDecimal stockAtHand;

    public CanteenItemDTO(CanteenItem canteenItem) {
        this.id = canteenItem.id;
        this.stockAtHand = canteenItem.stockAtHand;
        this.number = canteenItem.number;
        this.category = canteenItem.category;
        this.subCategory = canteenItem.subCategory;
        this.title = canteenItem.title;
        this.description = canteenItem.description;
        this.costPrice = canteenItem.costPrice;
        this.sellingPrice = canteenItem.sellingPrice;
        this.unitOfMeasure = canteenItem.unitOfMeasure;
    }
}
