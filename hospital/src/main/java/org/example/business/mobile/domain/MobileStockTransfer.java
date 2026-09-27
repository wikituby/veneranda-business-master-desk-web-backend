package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_stock_transfer")
public class MobileStockTransfer extends PanacheEntity {
    @Column(nullable = false)
    public Long fromItemId;
    @Column(nullable = false)
    public Long toItemId;
    @Column
    public String fromItemName;
    @Column
    public String toItemName;
    @Column(nullable = false)
    public BigDecimal fromQuantity = BigDecimal.ZERO;
    @Column(nullable = false)
    public BigDecimal toQuantity = BigDecimal.ZERO;
    @Column
    public BigDecimal unitCost;
    @Column
    public BigDecimal sellingPrice;
    @Column
    public String note;
    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
