package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_stock_receipt")
public class MobileStockReceipt extends PanacheEntity {
    @Column(nullable = false)
    public Long itemId;
    @Column
    public String itemName;
    @Column
    public Long storeId;
    @Column(nullable = false)
    public BigDecimal quantity = BigDecimal.ZERO;
    @Column
    public BigDecimal unitCost = BigDecimal.ZERO;
    @Column
    public BigDecimal totalCost = BigDecimal.ZERO;
    @Column
    public BigDecimal sellingPrice;
    @Column
    public String brand;
    @Column
    public String batchNumber;
    @Column(nullable = false)
    public LocalDateTime receivedAt = LocalDateTime.now();
}
