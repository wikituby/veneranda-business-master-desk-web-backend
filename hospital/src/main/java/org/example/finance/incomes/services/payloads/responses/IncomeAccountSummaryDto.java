package org.example.finance.incomes.services.payloads.responses;

import java.math.BigDecimal;

public class IncomeAccountSummaryDto {
    public String accountType;
    public String label;
    public BigDecimal totalAmount;
    public long entryCount;
    public boolean readOnly;

    public IncomeAccountSummaryDto() {
    }

    public IncomeAccountSummaryDto(String accountType, String label, BigDecimal totalAmount, long entryCount, boolean readOnly) {
        this.accountType = accountType;
        this.label = label;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.entryCount = entryCount;
        this.readOnly = readOnly;
    }
}