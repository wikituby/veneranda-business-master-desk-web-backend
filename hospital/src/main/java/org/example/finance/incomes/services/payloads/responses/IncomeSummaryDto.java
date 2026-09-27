package org.example.finance.incomes.services.payloads.responses;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class IncomeSummaryDto {
    public List<IncomeAccountSummaryDto> accounts = new ArrayList<>();
    public BigDecimal totalIncome = BigDecimal.ZERO;
    public String dateFrom;
    public String dateTo;
}