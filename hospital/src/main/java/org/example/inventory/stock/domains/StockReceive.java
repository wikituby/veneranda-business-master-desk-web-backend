package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class StockReceive extends PanacheEntity {

    @Column(nullable = false)
    public Long stockBatchId;

    @Column
    public String stockItemName;

    @Column(nullable = false)
    public Long storeId;

    @Column
    public String storeName;

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
    public String supplierName;

    @Column
    public Long supplierId;

    @Column
    public String invoiceNumber;

    @Column
    public String batchNumber;

    @Column
    public String packaging;
}
