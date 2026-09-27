package org.example.inventory.stock.services.payloads.requests;

import java.math.BigDecimal;

public class UnitSellingModelRequest {
    public String name;
    public BigDecimal unitSellingPrice;
    public Integer unitsInBundle;
    public BigDecimal bundlePrice;
    public BigDecimal profitMargin;
    public Integer sortOrder;
    public Boolean isDefault;
}
