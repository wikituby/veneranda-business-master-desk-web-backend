package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class StrengthUnit extends PanacheEntity {

    @Column
    public String title;

    @Column
    public String standardAbbreviation;

    @Column
    public String description;

    /** Short label for prescribing (e.g. mg); falls back to title when blank. */
    public String prescribingLabel() {
        if (standardAbbreviation != null && !standardAbbreviation.isBlank()) {
            return standardAbbreviation.trim();
        }
        return title != null ? title.trim() : null;
    }

}
