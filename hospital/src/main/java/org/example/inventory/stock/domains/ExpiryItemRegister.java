package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class ExpiryItemRegister extends PanacheEntity {

    @Column(nullable = false)
    public Long stockItemId;

    /** Stock batch that was written off (for audit and lookup). */
    @Column
    public Long stockBatchId;

    @Column(length = 120)
    public String batchNumber;

    @Column(precision = 19, scale = 2)
    public BigDecimal stockAtHand;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime dateOfStockRemoval;

    /** User or staff identifier who removed the stock. */
    @Column(length = 160)
    public String removedBy;
}
