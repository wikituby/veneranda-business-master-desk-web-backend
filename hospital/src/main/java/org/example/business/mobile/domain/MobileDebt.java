package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_debt")
public class MobileDebt extends PanacheEntity {
    @Column
    public Long storeId;
    @Column
    public Long saleId;
    @Column(nullable = false)
    public String customerName = "";
    @Column
    public String phone;
    @Column
    public String address;
    @Column(nullable = false)
    public BigDecimal amount = BigDecimal.ZERO;
    @Column(nullable = false)
    public boolean paid;
    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
