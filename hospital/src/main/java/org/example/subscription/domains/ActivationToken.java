package org.example.subscription.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "activation_token")
public class ActivationToken extends PanacheEntity {

    @Column(unique = true, nullable = false, length = 512)
    public String token;

    /** SHA-256 fingerprint of the exact pasted activation code (one-time use). */
    @Column(unique = true, length = 64)
    public String tokenHash;

    @Column
    public String facilityName;

    @Column(length = 500)
    public String facilityAddress;

    @Column(length = 2000)
    public String subscribedModuleKeys;

    @Column
    public Integer durationMonths = 12;

    /** When set, takes precedence over durationMonths on activation. */
    @Column
    public Integer durationDays;

    @Column
    public String status = "unused";

    @Column
    public LocalDateTime usedAt;

    @Column
    public Long usedByUserId;
}
