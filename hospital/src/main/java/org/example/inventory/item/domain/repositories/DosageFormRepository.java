package org.example.inventory.item.domain.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.inventory.item.domain.DosageForm;

@ApplicationScoped
public class DosageFormRepository implements PanacheRepository<DosageForm> {
}
