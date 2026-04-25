package org.example.finance.expenses.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.example.user.domains.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class ExpenseTransaction extends PanacheEntity {
    @ManyToOne
    @JoinColumn(nullable = false)
    public User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    public ExpenseAccount expenseAccount;

    @Column
    public String expenseAccountName;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfExpenseTransaction;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate upDateOfExpenseTransaction;

    @Column
    public LocalTime timeOfExpenseTransaction;

    @Column
    public BigDecimal amountTransacted;

    @Column
    public String receiver;

    @Column
    public String userName;

    @Column
    public String referenceNumber;

    @Column(columnDefinition = "TEXT") // Assuming this may be a longer text
    public String description;


}






