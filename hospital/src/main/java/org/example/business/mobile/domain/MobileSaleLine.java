package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "mobile_sale_line")
public class MobileSaleLine extends PanacheEntity {
    @Column(nullable = false)
    public Long saleId;
    @Column(nullable = false)
    public Long itemId;
    @Column
    public String itemName;
    @Column
    public String itemUnit;
    @Column
    public String itemCategory;
    @Column(nullable = false)
    public BigDecimal quantity = BigDecimal.ZERO;
    @Column(nullable = false)
    public BigDecimal unitPrice = BigDecimal.ZERO;
    @Column
    public BigDecimal productDiscount = BigDecimal.ZERO;
    @Column
    public BigDecimal lineTotal = BigDecimal.ZERO;
}
