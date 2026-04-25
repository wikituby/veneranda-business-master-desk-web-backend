package org.example.course.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class Course extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String title;

    @Column(unique = true, nullable = false)
    public String description;

    @JsonbTransient
    @Column(unique = true, nullable = true)
    public String details;

    @Column(unique = true, nullable = true)
    public String image;

    @Column(unique = true, nullable = true)
    public String parent;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate lastUpdateDate;

}






