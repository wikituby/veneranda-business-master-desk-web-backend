package org.example.finance.expenses.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class ExpenseAccount extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    public ExpenseCategory category;

    @Column(nullable = false)
    public String accountName;

    @Column(nullable = false)
    public String expenseCategoryName;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    public BigDecimal accountLimit;

    @Column
    public String limitPeriod;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfAccountCreation;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfAccountUpdate;

    @Column
    public LocalTime timeOfAccountCreation;
}






