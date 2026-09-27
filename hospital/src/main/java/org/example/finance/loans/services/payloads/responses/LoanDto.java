package org.example.finance.loans.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.finance.loans.domains.Loan;
import org.example.finance.loans.services.LoanService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanDto {
    public Long id;
    public Long recordedByUserId;
    public String recordedByUserName;
    public String borrowerName;
    public String borrowerContact;
    public String borrowerType;
    public BigDecimal principalAmount;
    public BigDecimal amountRepaid;
    public BigDecimal balanceOutstanding;
    public String status;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate loanDate;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dueDate;

    public String referenceNumber;
    public String description;
    public String repaymentNotes;

    public LoanDto(Loan loan) {
        if (loan == null) {
            return;
        }
        this.id = loan.id;
        this.recordedByUserId = loan.recordedByUserId;
        this.recordedByUserName = loan.recordedByUserName;
        this.borrowerName = loan.borrowerName;
        this.borrowerContact = loan.borrowerContact;
        this.borrowerType = loan.borrowerType;
        this.principalAmount = loan.principalAmount;
        this.amountRepaid = loan.amountRepaid != null ? loan.amountRepaid : BigDecimal.ZERO;
        this.balanceOutstanding = LoanService.balanceOutstanding(loan);
        this.status = LoanService.resolveStatus(loan);
        this.loanDate = loan.loanDate;
        this.dueDate = loan.dueDate;
        this.referenceNumber = loan.referenceNumber;
        this.description = loan.description;
        this.repaymentNotes = loan.repaymentNotes;
    }
}
