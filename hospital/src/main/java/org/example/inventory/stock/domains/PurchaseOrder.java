package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class PurchaseOrder extends PanacheEntity {

    @Column
    public String orderNo;

    @Column
    public Long supplierId;

    @Column
    public String supplierName;

    @Column(length = 2000)
    public String notes;

    @Column
    public String status;

    @Column
    public LocalDateTime createdAt;
}
