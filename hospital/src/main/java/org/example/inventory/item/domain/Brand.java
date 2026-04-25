package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

/**
 * Represents a pharmaceutical brand (e.g. P-Alaxin, Coartem, Panadol)
 * that can have multiple active ingredients with defined strengths per unit
 * and a single formulation (e.g., Tablet, Capsule, Suspension).
 */
@Entity
public class Brand extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String name;

    @Column
    public String manufacturer;

    @Column
    public String manufacturerAddress;

    @Column
    public String countryOfOrigin;

    @Column
    public String description;

}

