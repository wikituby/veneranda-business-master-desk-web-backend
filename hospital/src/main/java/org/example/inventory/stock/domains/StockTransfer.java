package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.example.inventory.store.domains.Store;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class StockTransfer extends PanacheEntity {


    public Long fromStoreId;

    @Column
    public Long toStoreId;

    @Column
    public Long stockBatchId;

    @Column(nullable = false)
    public BigDecimal quantity;

    @Column(nullable = false)
    public LocalDateTime transferDate = LocalDateTime.now();

    @Column
    public String transferredBy;
}

