package org.example.cafeteria.sales.saleDone.services.payloads.responses;

import org.example.cafeteria.sales.saleDone.domains.Sale;

import java.math.BigDecimal;

public class SaleDTO {
    public Long id;
    public BigDecimal quantity;
    public BigDecimal unitSellingPrice;
    public BigDecimal totalAmount;
    public String itemName;


    public SaleDTO(Sale sale) {
        this.id = sale.id;
        this.quantity = sale.quantity;
        this.unitSellingPrice = sale.unitSellingPrice;
        this.totalAmount = sale.totalAmount;
        this.itemName = sale.itemName;

    }
}






