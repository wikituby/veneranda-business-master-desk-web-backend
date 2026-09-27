package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_expense")
public class MobileExpense extends PanacheEntity {
    @Column
    public Long storeId;
    @Column(nullable = false)
    public BigDecimal amount = BigDecimal.ZERO;
    @Column
    public String description;
    @Column(nullable = false)
    public String receiver = "";
    @Column
    public Long expenseAccountId;
    @Column
    public String expenseAccountName;
    @Column
    public String referenceNumber;
    @Column
    public String userName;
    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
