package org.example.subscription.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "health_facility")
public class HealthFacility extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Column(length = 500)
    public String address;

    @Column
    public String contact;

    @Column
    public String status = "active";
}
