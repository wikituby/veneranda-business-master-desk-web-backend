package org.example.finance.invoice.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.client.domains.Patient;
import org.example.visit.domains.PatientVisit;
import org.example.finance.payments.cash.domains.Payments;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Invoice extends PanacheEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    public PatientVisit visit;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    public Patient patient;

    @Column
    public LocalTime timeOfCreation;

    @Column
    public String invoiceNo;

    @Column
    public String invoiceStatus;

    @Column
    public int invoicePlainNo;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfInvoice;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate upDateOfInvoice;

    @Column
    public LocalTime updateTimeOfCreation;

    @Column
    public BigDecimal balanceDue;

    @Column
    public String tin;

    @Column
    public String companyLogo;

    @Column
    public String documentTitle;

    @Column
    public String fromName;

    @Column
    public String toName;

    @Column
    public String fromAddress;

    @Column
    public String toAddress;

    @Column
    public String reference;

    @Column
    public BigDecimal subTotal;

    @Column
    public BigDecimal discount;

    @Column
    public BigDecimal tax;

    @Column
    public BigDecimal totalAmount;

    @Column
    public BigDecimal amountPaid;

    @Column
    public String notes;

    @OneToMany(mappedBy = "invoice",fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payments> payments = new ArrayList<>();

    public List<Payments> getPayments() {
        return payments;
    }

    public void setPayments(List<Payments> payments) {
        this.payments = payments;
    }

    public Long getId() {
        return this.id;
    }


    public BigDecimal getTotalBalanceDue() {
        return balanceDue;
    }

    public BigDecimal getBalanceDue() {
        return balanceDue;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }
}






