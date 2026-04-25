package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "ItemCategoryTable")
public class ItemCategory extends PanacheEntity {

    @Column
    public String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    public ItemCategory parent;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate lastUpdatedDate;
}


