package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_loan_payment")
public class MobileLoanPayment extends PanacheEntity {
    @Column(nullable = false)
    public Long loanId;
    @Column
    public Long clientId;
    @Column
    public Long storeId;
    @Column(nullable = false)
    public BigDecimal amount = BigDecimal.ZERO;
    @Column
    public String note;
    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
