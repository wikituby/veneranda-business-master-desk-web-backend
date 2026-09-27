package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.ItemUnitSellingModel;

import java.math.BigDecimal;

public class ItemUnitSellingModelDTO {
    public Long id;
    public Long itemId;
    public String name;
    public BigDecimal unitSellingPrice;
    public Integer unitsInBundle;
    public BigDecimal bundlePrice;
    public BigDecimal profitMargin;
    public Integer sortOrder;
    public Boolean isDefault;

    public ItemUnitSellingModelDTO() {
    }

    public ItemUnitSellingModelDTO(ItemUnitSellingModel entity) {
        this.id = entity.id;
        this.itemId = entity.item != null ? entity.item.id : null;
        this.name = entity.name;
        this.unitSellingPrice = entity.unitSellingPrice;
        this.unitsInBundle = entity.unitsInBundle;
        this.bundlePrice = entity.bundlePrice;
        this.profitMargin = entity.profitMargin;
        this.sortOrder = entity.sortOrder;
        this.isDefault = entity.isDefault;
    }
}
