package org.example.inventory.stock.domains;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class StockSupplier extends PanacheEntity {

    @Column
    public String supplierName;

    @Column
    public String emailAddress;

    @Column
    public String contact;

    @Column
    public String physicalAddress;

    @Column
    public String webSiteAddress;

    @Column
    public String abbreviation;

    @Column
    public String description;

    @Column
    public LocalDateTime creationDateTime;

    @Column
    public LocalDateTime upDateTime;
}
