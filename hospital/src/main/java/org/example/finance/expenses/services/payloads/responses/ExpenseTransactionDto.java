package org.example.finance.expenses.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.finance.expenses.domains.ExpenseTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ExpenseTransactionDto {
    public Long id;
    public Long userId;
    public Long expenseAccountId;
    public String expenseAccountName;
    public String userName;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfExpenseTransaction;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate upDateOfExpenseTransaction;
    
    public LocalTime timeOfExpenseTransaction;
    public BigDecimal amountTransacted;
    public String receiver;
    public String referenceNumber;
    public String description;

    public ExpenseTransactionDto(ExpenseTransaction expenseTransaction) {
        if (expenseTransaction != null) {
            this.id = expenseTransaction.id;
            this.userId = expenseTransaction.user != null ? expenseTransaction.user.id : null;
            this.expenseAccountId = expenseTransaction.expenseAccount != null ? expenseTransaction.expenseAccount.id : null;
            this.expenseAccountName = expenseTransaction.expenseAccountName;
            this.userName = expenseTransaction.userName;
            this.dateOfExpenseTransaction = expenseTransaction.dateOfExpenseTransaction;
            this.upDateOfExpenseTransaction = expenseTransaction.upDateOfExpenseTransaction;
            this.timeOfExpenseTransaction = expenseTransaction.timeOfExpenseTransaction;
            this.amountTransacted = expenseTransaction.amountTransacted;
            this.receiver = expenseTransaction.receiver;
            this.referenceNumber = expenseTransaction.referenceNumber;
            this.description = expenseTransaction.description;
        }
    }
}










