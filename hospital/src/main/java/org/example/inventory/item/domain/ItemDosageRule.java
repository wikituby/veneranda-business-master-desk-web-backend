package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class ItemDosageRule extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    public Item item;

    @Column(nullable = false, length = 180)
    public String title;

    @Column(length = 180)
    public String matchLabel;

    @Column(length = 300)
    public String ruleLabel;

    /** FIXED, WEIGHT_PER_DOSE, or WEIGHT_PER_DAY. */
    @Column(length = 40)
    public String doseCalculationType;

    @Column(precision = 19, scale = 4)
    public BigDecimal fixedDoseValue;

    @Column(length = 60)
    public String fixedDoseUnit;

    @Column(precision = 19, scale = 4)
    public BigDecimal weightDoseValue;

    @Column(length = 60)
    public String weightDoseUnit;

    @Column(precision = 19, scale = 4)
    public BigDecimal maxDoseValue;

    @Column(length = 60)
    public String maxDoseUnit;

    @Column(precision = 19, scale = 2)
    public BigDecimal minAgeYears;

    @Column(precision = 19, scale = 2)
    public BigDecimal maxAgeYears;

    @Column(precision = 19, scale = 2)
    public BigDecimal minWeightKg;

    @Column(precision = 19, scale = 2)
    public BigDecimal maxWeightKg;

    @Column(length = 80)
    public String route;

    @Column(precision = 19, scale = 4)
    public BigDecimal frequencyValue;

    /** Day-based period unit: minute = 1/1440, hour = 1/24, day = 1, week = 7, month = 30. */
    @Column(precision = 19, scale = 8)
    public BigDecimal frequencyUnit;

    @Column(precision = 19, scale = 4)
    public BigDecimal durationValue;

    /** Day-based period unit: minute = 1/1440, hour = 1/24, day = 1, week = 7, month = 30. */
    @Column(precision = 19, scale = 8)
    public BigDecimal durationUnit;

    @Column(precision = 19, scale = 4)
    public BigDecimal quantity;

    @Column(length = 80)
    public String quantityUnit;

    @Column(precision = 19, scale = 4)
    public BigDecimal stockUnitsPerDose;

    @Column(length = 80)
    public String stockUnitLabel;

    @Column(columnDefinition = "TEXT")
    public String instructions;

    @Column(columnDefinition = "TEXT")
    public String specialScheduleText;

    @Column(columnDefinition = "TEXT")
    public String warning;

    @Column
    public Boolean active = true;

    @Column
    public Integer sortOrder;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;
}
