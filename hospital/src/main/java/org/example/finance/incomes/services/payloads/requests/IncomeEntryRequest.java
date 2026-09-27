package org.example.finance.incomes.services.payloads.requests;

import java.math.BigDecimal;

public class IncomeEntryRequest {
    public Long recordedByUserId;
    public Long incomeAccountId;
    public String incomeAccountType;
    public BigDecimal amount;
    public String incomeDate;
    public Long incomeSourceId;
    public String sourceName;
    public Long incomePaymentMethodId;
    public String paymentMethod;
    public String referenceNumber;
    public Boolean electronicReceipt;
    public String description;
}
