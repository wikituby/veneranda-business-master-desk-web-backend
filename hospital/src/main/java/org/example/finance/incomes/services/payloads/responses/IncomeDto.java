package org.example.finance.incomes.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.finance.incomes.domains.IncomeEntry;
import org.example.finance.incomes.services.IncomeService;
import java.math.BigDecimal;
import java.time.LocalDate;

public class IncomeDto {
    public Long id;
    public Long incomeAccountId;
    public String incomeAccountType;
    public String incomeAccountLabel;
    public String incomeAccountName;
    public String incomeCategoryName;
    public BigDecimal amount;
    public boolean readOnly;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate incomeDate;

    public Long incomeSourceId;
    public String sourceName;
    public Long incomePaymentMethodId;
    public String paymentMethod;
    public String referenceCode;
    public String referenceNumber;
    public Boolean electronicReceipt;
    public String description;
    public Long recordedByUserId;
    public String recordedByUserName;

    public IncomeDto(IncomeEntry entry) {
        if (entry == null) {
            return;
        }
        this.id = entry.id;
        this.incomeAccountId = entry.incomeAccount != null ? entry.incomeAccount.id : null;
        this.incomeAccountType = entry.incomeAccountType;
        this.incomeAccountName = entry.incomeAccountName;
        this.incomeCategoryName = entry.incomeCategoryName;
        this.incomeAccountLabel = labelFor(entry);
        this.amount = entry.amount != null ? entry.amount : BigDecimal.ZERO;
        this.readOnly = false;
        this.incomeDate = entry.incomeDate;
        this.incomeSourceId = entry.incomeSourceId;
        this.sourceName = entry.sourceName;
        this.incomePaymentMethodId = entry.incomePaymentMethodId;
        this.paymentMethod = entry.paymentMethod;
        this.referenceCode = entry.referenceCode;
        this.referenceNumber = entry.referenceNumber;
        this.electronicReceipt = entry.electronicReceipt;
        this.description = entry.description;
        this.recordedByUserId = entry.recordedByUserId;
        this.recordedByUserName = entry.recordedByUserName;
    }

    private static String labelFor(IncomeEntry entry) {
        if (entry.incomeAccountName != null && !entry.incomeAccountName.isBlank()) {
            return entry.incomeAccountName;
        }
        return IncomeService.accountLabel(entry.incomeAccountType);
    }
}
