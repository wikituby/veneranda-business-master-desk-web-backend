package org.example.finance.incomes.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.finance.incomes.domains.IncomeCategory;
import java.time.LocalDate;

public class IncomeCategoryDto {
    public Long id;
    public String categoryName;
    public String description;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfCategoryCreation;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfCategoryUpdate;

    public IncomeCategoryDto(IncomeCategory category) {
        if (category == null) {
            return;
        }
        this.id = category.id;
        this.categoryName = category.categoryName;
        this.description = category.description;
        this.dateOfCategoryCreation = category.dateOfCategoryCreation;
        this.dateOfCategoryUpdate = category.dateOfCategoryUpdate;
    }
}
