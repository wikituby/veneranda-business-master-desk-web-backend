package org.example.inventory.stock.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.inventory.stock.domains.StockBatch;

@ApplicationScoped
public class StockBatchRepository implements PanacheRepository<StockBatch> {
}
