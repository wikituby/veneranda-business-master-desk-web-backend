package org.example.user.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    @Column(unique = true,nullable = false)
    public String username;

    @Column(unique = true,nullable = false)
    public String email;

    @JsonbTransient
    @Column(unique = true,nullable = false)
    public String password;

    @Column
    public String role;

    /** Comma-separated additional roles (primary role remains in {@link #role}). */
    @Column(length = 1000)
    public String secondaryRoles;

    @Column
    public String qualification;

    @Column
    public String registrationNumber;

    @Column
    public String status;

    @Column
    public String profilePic;

    @Column
    public String contact;

    /** Comma-separated hospital module (department) ids this user may access in queue. */
    @Column(length = 2000)
    public String assignedModuleIds;

    /** Comma-separated hospital clinic (room) ids this user may access in queue. */
    @Column(length = 2000)
    public String assignedClinicIds;

    /** Comma-separated overview page route keys this user may access. */
    @Column(length = 4000)
    public String allowedPageRoutes;

    /** Comma-separated page.component keys to hide (Access Control sub-component restrictions). */
    @Column(length = 4000)
    public String hiddenPageComponents;

    /** Health facility this user belongs to (multi-tenant subscription). */
    @Column
    public Long facilityId;

    /**
     * Locked default for patient registry page size (8/15/25/50/100).
     * Null means no saved default — UI uses the built-in default.
     */
    @Column
    public Integer registryItemsPerPage;

    public User() {
    }

    public User(String username, String email, String password, String role, String profilePic) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profilePic = profilePic;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /** Primary role plus secondary roles (deduplicated, order preserved). */
    public java.util.Set<String> allRoleNames() {
        java.util.LinkedHashSet<String> roles = new java.util.LinkedHashSet<>();
        if (role != null && !role.isBlank()) {
            roles.add(role.trim());
        }
        if (secondaryRoles != null && !secondaryRoles.isBlank()) {
            for (String part : secondaryRoles.split(",")) {
                String trimmed = part == null ? "" : part.trim();
                if (!trimmed.isBlank()) {
                    roles.add(trimmed);
                }
            }
        }
        return roles;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}






