package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a formulation/dosage form for a product variant
 * e.g., Tablet, Capsule, Suspension, Oral Solution
 */
@Entity
public class Formulation extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String name; // e.g., "Tablet", "Capsule", "Suspension"

    @Column
    public String description; // optional description

}
