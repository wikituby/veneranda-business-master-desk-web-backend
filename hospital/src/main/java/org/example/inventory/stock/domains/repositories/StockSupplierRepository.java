package org.example.inventory.stock.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.inventory.stock.domains.StockSupplier;

@ApplicationScoped
public class StockSupplierRepository implements PanacheRepository<StockSupplier> {
}
