package org.example.cafeteria.inventory.stock.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.cafeteria.inventory.stock.domains.CanteenStock;

@ApplicationScoped
public class CanteenStockRepository implements PanacheRepository<CanteenStock> {

}
