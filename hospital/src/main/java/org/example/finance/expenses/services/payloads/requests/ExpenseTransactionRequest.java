package org.example.finance.expenses.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ExpenseTransactionRequest {
    @Schema(example = "1")
    public Long userId;

    @Schema(example = "1")
    public Long expenseAccountId;

    @Schema(example = "50000")
    public BigDecimal amountTransacted;

    @Schema(example = "John Doe")
    public String receiver;

    @Schema(example = "Payment for office supplies")
    public String description;

    @Schema(example = "2026-08-19T17:35")
    public String transactionDateTime;
}










