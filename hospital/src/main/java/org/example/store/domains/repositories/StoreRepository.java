package org.example.store.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.store.domains.Store;

@ApplicationScoped
public class StoreRepository implements PanacheRepository<Store> {
}






