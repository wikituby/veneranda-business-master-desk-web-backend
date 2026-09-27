package org.example.user.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "user_role",
        uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
public class UserRole extends PanacheEntity {

    @Column(nullable = false, length = 120)
    public String name;

    public UserRole() {
    }

    public UserRole(String name) {
        this.name = name;
    }
}
