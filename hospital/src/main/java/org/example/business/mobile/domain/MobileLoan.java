package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_loan")
public class MobileLoan extends PanacheEntity {
    @Column
    public Long storeId;
    @Column(nullable = false)
    public Long clientId;
    @Column(nullable = false)
    public BigDecimal principalAmount = BigDecimal.ZERO;
    @Column
    public BigDecimal annualInterestPercent = BigDecimal.ZERO;
    @Column
    public String interestType = "simple";
    @Column
    public LocalDate expectedPaymentDate;
    @Column
    public BigDecimal interestAmount = BigDecimal.ZERO;
    @Column
    public BigDecimal totalDue = BigDecimal.ZERO;
    @Column
    public BigDecimal amountPaid = BigDecimal.ZERO;
    @Column
    public String note;
    @Column
    public String status = "active";
    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
