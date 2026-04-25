package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class RouteOfAdmin extends PanacheEntity {

    @Column
    public String title;

    @Column
    public String standardAbbreviation;

    @Column
    public String description;
}
