package org.example.inventory.item.domain.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.inventory.item.domain.Strength;

@ApplicationScoped
public class StrengthRepository implements PanacheRepository<Strength> {
}
