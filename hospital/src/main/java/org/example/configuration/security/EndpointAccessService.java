package org.example.configuration.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.user.domains.RoleEndpointAccess;
import org.example.user.domains.User;
import org.example.user.domains.repositories.RoleEndpointAccessRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class EndpointAccessService {

    @Inject
    RoleEndpointAccessRepository roleEndpointAccessRepository;

    public boolean isPrivilegedRole(String role) {
        String r = normalizeRole(role);
        return "admin".equals(r) || "md".equals(r);
    }

    public boolean canAccessEndpoint(User user, String endpointKey) {
        if (user == null || endpointKey == null || endpointKey.isBlank()) {
            return false;
        }
        if (isPrivilegedRole(user.role)) {
            return true;
        }
        Set<String> allowed = effectiveEndpointKeysForUser(user);
        String role = normalizeRole(user.role);
        boolean rolePolicyExists = roleEndpointAccessRepository.findByRoleNormalized(role).isPresent();
        boolean userHasPageRestrictions = !parsePageKeys(user.allowedPageRoutes).isEmpty();
        if (!rolePolicyExists && !userHasPageRestrictions) {
            // No role policy and no per-user page limits — keep legacy behaviour.
            return true;
        }
        return allowed.contains(endpointKey.trim());
    }

    /** Role keys plus endpoint keys implied by the user's assigned overview pages. */
    public Set<String> effectiveEndpointKeysForUser(User user) {
        Set<String> keys = new HashSet<>();
        if (user == null) {
            return keys;
        }
        String role = normalizeRole(user.role);
        roleEndpointAccessRepository.findByRoleNormalized(role).ifPresent(row -> {
            keys.addAll(EndpointAccessCatalog.parseEndpointKeysCsv(row.allowedEndpointKeys));
        });
        keys.addAll(EndpointAccessCatalog.endpointKeysForPages(parsePageKeys(user.allowedPageRoutes)));
        return keys;
    }

    public Set<String> endpointKeysForRole(String role) {
        return roleEndpointAccessRepository.findByRoleNormalized(role)
                .map(row -> new HashSet<>(EndpointAccessCatalog.parseEndpointKeysCsv(row.allowedEndpointKeys)))
                .orElseGet(HashSet::new);
    }

    public RoleEndpointAccess upsertRoleEndpoints(String role, Iterable<String> endpointKeys) {
        String normalized = RoleEndpointAccessRepository.normalizeRole(role);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }
        RoleEndpointAccess row = roleEndpointAccessRepository.findByRoleNormalized(normalized)
                .orElseGet(() -> {
                    RoleEndpointAccess created = new RoleEndpointAccess();
                    created.role = normalized;
                    return created;
                });
        row.allowedEndpointKeys = EndpointAccessCatalog.endpointKeysToCsv(endpointKeys);
        roleEndpointAccessRepository.persist(row);
        return row;
    }

    private Set<String> parsePageKeys(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase(Locale.ROOT);
    }
}
