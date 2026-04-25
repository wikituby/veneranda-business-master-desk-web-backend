package org.example.cafeteria.sales.saleDone.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.example.cafeteria.sales.saleDay.domains.SaleDay;

import java.math.BigDecimal;

@Entity
@Table(name = "SaleDone")
public class Sale extends PanacheEntity {

    ///////////////////NB: replaced treatment requested//////////////////////////////////////////////////////////////
    @ManyToOne
    @JoinColumn(nullable = false)
    public SaleDay saleDay;

    // Quantity of lab tests requested
    @Column(nullable = false)
    public BigDecimal quantity;

    // Unit price of the lab test
    @Column(nullable = false)
    public BigDecimal unitSellingPrice;

    // Total amount for the requested lab tests (unitPrice * quantity)
    @Column(nullable = false)
    public BigDecimal totalAmount;

    // Reference to the specific lab test being requested
    @Column(nullable = false)
    public String itemName;

    @Column
    public String status;



}






