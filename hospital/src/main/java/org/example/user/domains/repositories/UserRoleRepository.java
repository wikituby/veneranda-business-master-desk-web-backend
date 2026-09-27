package org.example.user.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.user.domains.UserRole;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRoleRepository implements PanacheRepository<UserRole> {

    public Optional<UserRole> findByNameIgnoreCase(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return find("lower(name) = ?1", name.trim().toLowerCase()).firstResultOptional();
    }

    public List<UserRole> findAllOrdered() {
        return list("order by lower(name)");
    }

    public boolean existsByNameIgnoreCase(String name) {
        return findByNameIgnoreCase(name).isPresent();
    }
}
