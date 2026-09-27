package org.example.queue.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class HospitalModule extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String code;

    @Column(nullable = false)
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(nullable = false)
    public boolean active = true;

    @Column
    public Integer sortOrder;

    /** Optional route key used by the frontend (e.g. lab, scan, reception). */
    @Column
    public String routeKey;

    /**
     * Comma-separated role names allowed to access this module.
     * If null/blank => accessible to all roles.
     * Example: "md,admin,receptionist"
     */
    @Column(columnDefinition = "TEXT")
    public String allowedRoles;
}
