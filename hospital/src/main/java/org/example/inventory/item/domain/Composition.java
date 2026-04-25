package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Represents the link between a product variant and its active ingredient,
 * including the specific strength per dosage unit.
 */
@Entity
public class Composition extends PanacheEntity {

    @Column
    public Long stockItemId;

    @Column
    public Long strengthId;
}

