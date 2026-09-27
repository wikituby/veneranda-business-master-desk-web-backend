package org.example.user.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.example.auth.services.payloads.RoleResponse;
import org.example.statics.RoleEnums;
import org.example.user.domains.UserRole;
import org.example.user.domains.repositories.UserRepository;
import org.example.user.domains.repositories.UserRoleRepository;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleService {

    private static final String[] DEFAULT_ROLES = {
            "admin", "customer", "agent", "md", "sono", "lab", "clinician", "doctor", "pharmacist", "pharmacy"
    };

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    UserRepository userRepository;

    @Transactional
    public RoleResponse getAllRoles() {
        syncCatalog();
        return toResponse(userRoleRepository.findAllOrdered());
    }

    @Transactional
    public RoleResponse createRole(String rawName) {
        String name = normalizeName(rawName);
        if (userRoleRepository.existsByNameIgnoreCase(name)) {
            throw conflict("Role already exists.");
        }
        userRoleRepository.persist(new UserRole(name));
        return toResponse(userRoleRepository.findAllOrdered());
    }

    @Transactional
    public RoleResponse updateRole(String rawOldName, String rawNewName) {
        String oldName = normalizeName(rawOldName);
        String newName = normalizeName(rawNewName);

        if (oldName.equalsIgnoreCase(newName)) {
            return toResponse(userRoleRepository.findAllOrdered());
        }

        UserRole existing = userRoleRepository.findByNameIgnoreCase(oldName)
                .orElseThrow(() -> notFound("Role not found."));

        if (userRoleRepository.existsByNameIgnoreCase(newName)) {
            throw conflict("A role with that name already exists.");
        }

        existing.name = newName;
        userRoleRepository.persist(existing);
        userRepository.renameRoleForUsers(oldName, newName);

        return toResponse(userRoleRepository.findAllOrdered());
    }

    @Transactional
    public RoleResponse deleteRole(String rawName) {
        String name = normalizeName(rawName);
        UserRole existing = userRoleRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> notFound("Role not found."));

        long usersWithRole = userRepository.countByRoleIgnoreCase(name);
        if (usersWithRole > 0) {
            throw conflict("Cannot delete role while " + usersWithRole + " user(s) still use it.");
        }

        userRoleRepository.delete(existing);
        return toResponse(userRoleRepository.findAllOrdered());
    }

    private void syncCatalog() {
        Set<String> names = new LinkedHashSet<>();
        Arrays.stream(RoleEnums.values()).map(Enum::name).forEach(names::add);
        Arrays.stream(DEFAULT_ROLES).forEach(names::add);
        userRepository.findDistinctRoles().stream()
                .map(this::normalizeName)
                .forEach(names::add);

        for (String name : names) {
            if (!userRoleRepository.existsByNameIgnoreCase(name)) {
                userRoleRepository.persist(new UserRole(name));
            }
        }
    }

    private RoleResponse toResponse(List<UserRole> roles) {
        Set<String> names = roles.stream()
                .map(role -> role.name)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new RoleResponse(names);
    }

    private String normalizeName(String raw) {
        if (raw == null || raw.isBlank()) {
            throw badRequest("Role name is required.");
        }
        return raw.trim();
    }

    private WebApplicationException badRequest(String message) {
        return new WebApplicationException(message, 400);
    }

    private WebApplicationException notFound(String message) {
        return new WebApplicationException(message, 404);
    }

    private WebApplicationException conflict(String message) {
        return new WebApplicationException(message, 409);
    }
}
