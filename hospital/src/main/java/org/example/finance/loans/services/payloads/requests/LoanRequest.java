package org.example.finance.loans.services.payloads.requests;

import java.math.BigDecimal;

public class LoanRequest {
    public Long recordedByUserId;
    public String borrowerName;
    public String borrowerContact;
    public String borrowerType;
    public BigDecimal principalAmount;
    public String loanDate;
    public String dueDate;
    public String description;
}
