package org.example.cafeteria.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.example.cafeteria.inventory.item.domains.CanteenItem;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class CanteenStock extends PanacheEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)  // Auto-delete stock when item is deleted
    public CanteenItem item;

    @Column
    public String store;

    @Column(nullable = false)
    public BigDecimal quantityReceived;

    @Column(nullable = false)
    public BigDecimal unitCostPrice;

    @Column(nullable = false)
    public BigDecimal totalCostPrice;

    @Column(nullable = false)
    public BigDecimal unitSellingPrice;

    @Column(nullable = false)
    public BigDecimal quantityAvailable;

    @Column(nullable = false)
    public BigDecimal newQuantity;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate receiveDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;

    @Column
    public String brand;

    @Column
    public String packaging;
}
