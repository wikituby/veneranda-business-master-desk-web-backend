package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class ActiveIngredient extends PanacheEntity {

    @Column
    public String number;

    @Column(nullable = false)
    public String genericName;

    // The deepest relevant category (e.g. ANTIBIOTIC)
    @Column
    public Long categoryId;

    @Column(columnDefinition = "TEXT")
    public String indication;

    @Column
    public String categoryName;

    @Column(columnDefinition = "TEXT")
    public String contraIndication;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;

}
