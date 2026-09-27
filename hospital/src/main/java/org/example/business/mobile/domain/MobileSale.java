package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_sale")
public class MobileSale extends PanacheEntity {
    @Column
    public Long storeId;
    @Column(nullable = false)
    public BigDecimal totalAmount = BigDecimal.ZERO;
    @Column
    public BigDecimal overallDiscount = BigDecimal.ZERO;
    @Column
    public BigDecimal amountReceived = BigDecimal.ZERO;
    @Column
    public BigDecimal balance = BigDecimal.ZERO;
    @Column
    public String customerName;
    @Column
    public String customerPhone;
    @Column
    public String customerAddress;
    @Column
    public String paymentMethod = "cash";
    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
