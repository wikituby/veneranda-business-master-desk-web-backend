package org.example.inventory.item.domain;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Manufacturer extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String manufacturerName;

    @Column
    public String emailAddress;

    @Column
    public String contact;

    @Column
    public String physicalAddress;

    @Column
    public String webSiteAddress;

    @Column
    public String abbreviation;

    @Column
    public String countryOfOrigin;

    @Column
    public String description;

    @Column
    public LocalDateTime creationDateTime;

    @Column
    public LocalDateTime upDateTime;
}













