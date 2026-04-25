package org.example.inventory.store.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity(name = "InventoryStore")
@Table(name = "InventoryStoreTable")
public class Store extends PanacheEntity {

    @Column
    public String name;

    @Column
    public String location;

    @Column
    public String description;

    @Column
    public String defaultStatus;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

}


