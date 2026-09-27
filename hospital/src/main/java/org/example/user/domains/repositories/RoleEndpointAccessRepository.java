package org.example.user.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.user.domains.RoleEndpointAccess;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RoleEndpointAccessRepository implements PanacheRepository<RoleEndpointAccess> {

    public Optional<RoleEndpointAccess> findByRoleNormalized(String role) {
        if (role == null || role.isBlank()) {
            return Optional.empty();
        }
        return find("role", normalizeRole(role)).firstResultOptional();
    }

    public List<RoleEndpointAccess> findAllOrdered() {
        return listAll();
    }

    public static String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase();
    }
}
