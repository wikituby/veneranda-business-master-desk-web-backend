package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_debt_payment")
public class MobileDebtPayment extends PanacheEntity {
    @Column
    public Long debtId;
    @Column
    public Long saleId;
    @Column
    public String customerName;
    @Column(nullable = false)
    public BigDecimal amount = BigDecimal.ZERO;
    @Column
    public String note;
    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
    @Column
    public boolean reversed;
}
