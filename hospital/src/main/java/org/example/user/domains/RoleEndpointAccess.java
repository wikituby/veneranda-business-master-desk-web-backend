package org.example.user.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Per-role allowed API endpoint keys (comma-separated). */
@Entity
@Table(name = "role_endpoint_access")
public class RoleEndpointAccess extends PanacheEntity {

    @Column(nullable = false, unique = true, length = 120)
    public String role;

    /** Comma-separated keys from {@link org.example.configuration.security.EndpointAccessCatalog}. */
    @Column(length = 8000)
    public String allowedEndpointKeys;
}
