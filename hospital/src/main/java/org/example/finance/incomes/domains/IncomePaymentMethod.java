package org.example.finance.incomes.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
public class IncomePaymentMethod extends PanacheEntity {

    @Column(nullable = false)
    public String methodName;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateCreated;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateUpdated;
}
