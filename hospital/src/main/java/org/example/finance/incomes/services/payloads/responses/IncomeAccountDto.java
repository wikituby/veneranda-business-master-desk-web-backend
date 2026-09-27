package org.example.finance.incomes.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.finance.incomes.domains.IncomeAccount;
import java.time.LocalDate;

public class IncomeAccountDto {
    public Long id;
    public Long categoryId;
    public String accountName;
    public String incomeCategoryName;
    public String description;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfAccountCreation;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfAccountUpdate;

    public IncomeAccountDto(IncomeAccount account) {
        if (account == null) {
            return;
        }
        this.id = account.id;
        this.categoryId = account.category != null ? account.category.id : null;
        this.accountName = account.accountName;
        this.incomeCategoryName = account.incomeCategoryName;
        this.description = account.description;
        this.dateOfAccountCreation = account.dateOfAccountCreation;
        this.dateOfAccountUpdate = account.dateOfAccountUpdate;
    }
}
