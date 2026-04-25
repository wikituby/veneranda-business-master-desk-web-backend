package org.example.cafeteria.inventory.store.domains.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.cafeteria.inventory.store.domains.CanteenStore;

@ApplicationScoped
public class CanteenStoreRepository implements PanacheRepository<CanteenStore> {
}






