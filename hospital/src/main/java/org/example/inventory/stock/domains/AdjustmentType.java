package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class AdjustmentType extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    /** Stable code for logic / API, e.g. DAMAGED, EXPIRED */
    @Column(nullable = false, unique = true, length = 64)
    public String code;

    @Column
    public Boolean active = Boolean.TRUE;
}
