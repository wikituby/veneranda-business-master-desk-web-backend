package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_expense_account")
public class MobileExpenseAccount extends PanacheEntity {
    @Column(nullable = false)
    public String accountName = "";
    @Column
    public String description;
    @Column
    public Long categoryId;
    @Column
    public String categoryName;
    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
