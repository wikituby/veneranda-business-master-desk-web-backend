package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_service_txn")
public class MobileServiceTxn extends PanacheEntity {
    @Column
    public Long storeId;
    @Column(nullable = false)
    public String title = "";
    @Column
    public String description;
    @Column(nullable = false)
    public BigDecimal amount = BigDecimal.ZERO;
    @Column
    public String customerName;
    @Column
    public String paymentMethod;
    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
