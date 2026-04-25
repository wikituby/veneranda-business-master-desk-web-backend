package org.example.inventory.item.domain.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.inventory.item.domain.StrengthUnit;

@ApplicationScoped
public class StrengthUnitRepository implements PanacheRepository<StrengthUnit> {
}
