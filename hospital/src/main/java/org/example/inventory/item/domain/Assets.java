package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class Assets extends PanacheEntity {

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public double depreciationRate;

    @Column
    public int usefulLifeMonths;

    @Column
    public LocalDate acquisitionDate;

    @Column
    public String serialNumber;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;
}
