package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class StockConfidenceLevel extends PanacheEntity {

    @Column(nullable = false, unique = true, length = 40)
    public String code;

    @Column(nullable = false)
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(nullable = false)
    public Integer minimumScore;

    @Column(nullable = false)
    public Integer maximumScore;

    @Column
    public Boolean active = Boolean.TRUE;

    @Column
    public Integer displayOrder;

    @Column
    public LocalDateTime createdAt;

    @Column
    public LocalDateTime updatedAt;
}