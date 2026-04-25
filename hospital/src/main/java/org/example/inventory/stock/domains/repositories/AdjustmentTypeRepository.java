package org.example.inventory.stock.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.inventory.stock.domains.AdjustmentType;

@ApplicationScoped
public class AdjustmentTypeRepository implements PanacheRepository<AdjustmentType> {
}
