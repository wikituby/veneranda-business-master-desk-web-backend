package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class PurchaseOrderLine extends PanacheEntity {

    @Column
    public Long purchaseOrderId;

    @Column
    public Long stockItemId;

    @Column
    public String stockItemName;

    @Column
    public String storeName;

    @Column
    public String batchNumber;

    @Column
    public BigDecimal inStock;

    @Column
    public BigDecimal reorderTo;

    @Column
    public BigDecimal quantityOrdered;

    @Column
    public BigDecimal unitPrice;

    @Column(length = 500)
    public String lineNotes;
}
