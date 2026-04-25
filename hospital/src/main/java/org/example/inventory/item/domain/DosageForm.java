package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

/**
 * Represents a general dosage form, e.g., Tablet, Capsule, Suspension,
 * and links to a specific formulation.
 */
@Entity
public class DosageForm extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String name; // e.g., "Tablet", "Capsule", "Suspension"

    @Column
    public Long formulationId; // the specific formulation associated with this dosage form

    @Column
    public String description; // optional description
}
