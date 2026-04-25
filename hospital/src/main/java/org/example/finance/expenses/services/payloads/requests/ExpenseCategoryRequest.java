package org.example.finance.expenses.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ExpenseCategoryRequest {
    @Schema(example = "Operational Expenses")
    public String categoryName;

    @Schema(example = "Category for operational expenses")
    public String description;
}










