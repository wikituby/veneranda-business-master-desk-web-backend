package org.example.finance.expenses.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ExpenseAccountRequest {
    @Schema(example = "1")
    public Long categoryId;

    @Schema(example = "Office Supplies Account")
    public String accountName;

    @Schema(example = "Account for office supplies expenses")
    public String description;
}










