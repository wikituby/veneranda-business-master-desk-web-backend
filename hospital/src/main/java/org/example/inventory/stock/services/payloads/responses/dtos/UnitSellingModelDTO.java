package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.UnitSellingModel;

import java.math.BigDecimal;

public class UnitSellingModelDTO {
    public Long id;
    public Long stockItemId;
    public String name;
    public BigDecimal unitSellingPrice;
    public Integer unitsInBundle;
    public BigDecimal bundlePrice;
    public BigDecimal profitMargin;
    public Integer sortOrder;
    public Boolean isDefault;

    public UnitSellingModelDTO() {
    }

    public UnitSellingModelDTO(UnitSellingModel entity) {
        this.id = entity.id;
        this.stockItemId = entity.stockItem != null ? entity.stockItem.id : null;
        this.name = entity.name;
        this.unitSellingPrice = entity.unitSellingPrice;
        this.unitsInBundle = entity.unitsInBundle;
        this.bundlePrice = entity.bundlePrice;
        this.profitMargin = entity.profitMargin;
        this.sortOrder = entity.sortOrder;
        this.isDefault = entity.isDefault;
    }
}
