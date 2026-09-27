package org.example.finance.loans.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Loan extends PanacheEntity {

    /** FK to {@code user} table (same as {@link org.example.user.domains.User}). */
    @Column(name = "user_id")
    public Long recordedByUserId;

    @Column
    public String recordedByUserName;

    @Column(nullable = false)
    public String borrowerName;

    @Column
    public String borrowerContact;

    @Column
    public String borrowerType;

    @Column(nullable = false)
    public BigDecimal principalAmount;

    @Column(nullable = false)
    public BigDecimal amountRepaid;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate loanDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dueDate;

    @Column
    public String referenceNumber;

    @Column
    public String status;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(columnDefinition = "TEXT")
    public String repaymentNotes;
}
