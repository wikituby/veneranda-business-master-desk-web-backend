package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class StockInitialization extends PanacheEntity {

    @Column
    public Long stockBatchId;

    @Column
    public Long stockItemId;

    @Column
    public Long itemId;

    @Column
    public String itemName;

    @Column
    public Long storeId;

    @Column
    public String storeName;

    @Column(precision = 19, scale = 2)
    public BigDecimal draftQuantity;

    @Column(precision = 19, scale = 2)
    public BigDecimal initialQuantity;

    @Column(nullable = false)
    public boolean finalized = false;

    @Column
    public Long performedByUserId;

    @Column
    public String performedByUserName;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime finalizedAt;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime createdAt;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd'T'HH:mm:ss")
    public LocalDateTime updatedAt;
}
