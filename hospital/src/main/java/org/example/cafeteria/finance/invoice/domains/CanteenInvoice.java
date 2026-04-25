package org.example.cafeteria.finance.invoice.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.cafeteria.client.domains.Buyer;
import org.example.cafeteria.finance.payments.cash.domains.CanteenPayments;
import org.example.cafeteria.sales.saleDay.domains.SaleDay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CanteenInvoice extends PanacheEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    public SaleDay saleDay;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    public Buyer buyer;

    @Column
    public LocalTime timeOfCreation;

    @Column
    public String invoiceNo;

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
    private List<CanteenPayments> payments = new ArrayList<>();

    public List<CanteenPayments> getPayments() {
        return payments;
    }

    public void setPayments(List<CanteenPayments> payments) {
        this.payments = payments;
    }

    public Long getId() {
        return this.id;
    }




}






