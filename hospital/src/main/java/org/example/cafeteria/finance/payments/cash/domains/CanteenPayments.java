package org.example.cafeteria.finance.payments.cash.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.example.cafeteria.finance.invoice.domains.CanteenInvoice;
import org.example.cafeteria.sales.saleDay.domains.SaleDay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class CanteenPayments extends PanacheEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    public SaleDay visit;

    @ManyToOne
    @JoinColumn(nullable = false)
    public CanteenInvoice invoice;

    @Column
    public BigDecimal amountToPay;

    @Column
    public String paymentForm; // e.g., "cash at hand or cash at bank"

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfPayment;

    @Column
    public LocalTime timeOfPayment;

    @Column
    public String status; // approved or pending

    @Column
    public String notes;

    @Column
    public String paidBy;

    @Column
    public String receivedBy;


}






