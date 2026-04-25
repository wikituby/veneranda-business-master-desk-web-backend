package org.example.finance.expenses.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.finance.expenses.domains.ExpenseCategory;

import java.time.LocalDate;

public class ExpenseCategoryDto {
    public Long id;
    public String categoryName;
    public String description;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfCategoryCreation;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfCategoryUpdate;

    public ExpenseCategoryDto(ExpenseCategory expenseCategory) {
        if (expenseCategory != null) {
            this.id = expenseCategory.id;
            this.categoryName = expenseCategory.categoryName;
            this.description = expenseCategory.description;
            this.dateOfCategoryCreation = expenseCategory.dateOfCategoryCreation;
            this.dateOfCategoryUpdate = expenseCategory.dateOfCategoryUpdate;
        }
    }
}










