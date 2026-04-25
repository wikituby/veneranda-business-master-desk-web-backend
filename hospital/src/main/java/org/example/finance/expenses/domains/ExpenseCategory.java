package org.example.finance.expenses.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class ExpenseCategory extends PanacheEntity {

    @Column(nullable = false)
    public String categoryName;

    @Column
    public String description;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfCategoryCreation;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfCategoryUpdate;

}






