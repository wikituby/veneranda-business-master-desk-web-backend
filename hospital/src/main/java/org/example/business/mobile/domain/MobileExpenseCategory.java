package org.example.business.mobile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_expense_category")
public class MobileExpenseCategory extends PanacheEntity {
    @Column(nullable = false)
    public String name = "";
    @Column
    public String description;
    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
