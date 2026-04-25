package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class Strength extends PanacheEntity {

    @Column
    public Long activeIngredientId;

    @Column
    public BigDecimal strengthValue;

    @Column
    public Long strengthUnitId;
}
