package org.example.queue.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class HospitalClinic extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "hospital_module_id", nullable = false)
    public HospitalModule hospitalModule;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public boolean active = true;

    /**
     * Comma-separated role names allowed to access this clinic/room.
     * If null/blank => accessible to all roles (subject to module access).
     */
    @Column(columnDefinition = "TEXT")
    public String allowedRoles;
}
