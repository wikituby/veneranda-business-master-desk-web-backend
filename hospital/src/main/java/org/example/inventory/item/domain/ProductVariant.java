package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific formulation of a brand, e.g.:
 * - P-Alaxin Tablet 40/320 mg
 * - P-Alaxin Suspension 20/160 mg/5mL
 */
@Entity
public class ProductVariant extends PanacheEntity {

    @Column
    public Long dosageFormId; // e.g. "Tablet", "Suspension", "Capsule"

    @Column
    public Long formulationId; // e.g. "Coated tablet", "Oral suspension"

    @Column
    public Long brandId;


}
