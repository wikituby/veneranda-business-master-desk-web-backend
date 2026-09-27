package org.example.finance.expenses.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.finance.expenses.domains.ExpenseAccount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ExpenseAccountDto {
    public Long id;
    public Long categoryId;
    public String accountName;
    public String expenseCategoryName;
    public String description;
    public BigDecimal accountLimit;
    public String limitPeriod;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfAccountCreation;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfAccountUpdate;
    
    public LocalTime timeOfAccountCreation;

    public ExpenseAccountDto(ExpenseAccount expenseAccount) {
        if (expenseAccount != null) {
            this.id = expenseAccount.id;
            this.categoryId = expenseAccount.category != null ? expenseAccount.category.id : null;
            this.accountName = expenseAccount.accountName;
            this.expenseCategoryName = expenseAccount.expenseCategoryName;
            this.description = expenseAccount.description;
            this.accountLimit = expenseAccount.accountLimit;
            this.limitPeriod = expenseAccount.limitPeriod;
            this.dateOfAccountCreation = expenseAccount.dateOfAccountCreation;
            this.dateOfAccountUpdate = expenseAccount.dateOfAccountUpdate;
            this.timeOfAccountCreation = expenseAccount.timeOfAccountCreation;
        }
    }
}










