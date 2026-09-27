package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class VerificationStatus extends PanacheEntity {

    @Column(nullable = false, unique = true, length = 80)
    public String code;

    @Column(nullable = false)
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    public Boolean active = Boolean.TRUE;

    @Column
    public Integer displayOrder;

    @Column
    public LocalDateTime createdAt;

    @Column
    public LocalDateTime updatedAt;
}