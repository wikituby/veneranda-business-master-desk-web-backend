package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class ItemUnitSellingModel extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    public Item item;

    @Column(nullable = false, length = 120)
    public String name;

    @Column(nullable = false)
    public BigDecimal unitSellingPrice;

    @Column
    public Integer unitsInBundle;

    @Column
    public BigDecimal bundlePrice;

    @Column
    public BigDecimal profitMargin;

    @Column
    public Integer sortOrder;

    @Column
    public Boolean isDefault = false;
}
