package org.example.subscription.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "facility_subscription")
public class FacilitySubscription extends PanacheEntity {

    @Column(nullable = false)
    public Long facilityId;

    @Column
    public String status = "active";

    @Column(length = 2000)
    public String subscribedModuleKeys;

    @Column
    public LocalDateTime periodStart;

    @Column
    public LocalDateTime periodEnd;

    @Column
    public String activationTokenUsed;
}
