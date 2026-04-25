package org.example.store.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity(name = "GeneralStore")
@Table(name = "StoreTable")
public class Store extends PanacheEntity {

    @Column
    public String name;

    @Column
    public String location;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate lastUpdatedDate;
}








