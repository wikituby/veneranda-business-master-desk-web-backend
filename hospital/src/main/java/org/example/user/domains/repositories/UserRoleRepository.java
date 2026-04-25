package org.example.user.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.user.domains.UserRole;

@ApplicationScoped
public class UserRoleRepository implements PanacheRepository<UserRole> {

}






