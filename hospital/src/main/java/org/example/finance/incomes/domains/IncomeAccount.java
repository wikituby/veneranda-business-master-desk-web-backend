package org.example.finance.incomes.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class IncomeAccount extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    public IncomeCategory category;

    @Column(nullable = false)
    public String accountName;

    @Column(nullable = false)
    public String incomeCategoryName;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfAccountCreation;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfAccountUpdate;

    @Column
    public LocalTime timeOfAccountCreation;
}
