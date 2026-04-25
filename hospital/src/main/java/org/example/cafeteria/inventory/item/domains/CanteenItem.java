package org.example.cafeteria.inventory.item.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class CanteenItem extends PanacheEntity {

    @Column
    public String number;

    @Column
    public String category;

    @Column
    public String subCategory;

    @Column(nullable = false)
    public String title;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    public BigDecimal costPrice;

    @Column
    public BigDecimal sellingPrice;

    @Column
    public String image;

    @Column
    public BigDecimal stockAtHand;

    @Column
    public Integer reOrderLevel;

    @Column
    public String unitOfMeasure;

    @Column
    public String brand;

    @Column
    public String packaging;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;


}
