package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.Item;

import java.math.BigDecimal;

public class ItemDTO {
    public Long id;
    public String number;
    public Long categoryId;
    public String categoryName;
    public String genericName;
    public String description;



    public ItemDTO(Item item) {
        this.id = item.id;
        this.number = item.number;
        this.categoryId = item.category != null ? item.category.id : null;
        this.genericName = item.genericName;
        this.categoryName = item.category != null ? item.category.name : null;
        this.description = item.description;


    }
}
