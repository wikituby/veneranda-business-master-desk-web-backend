package org.example.inventory.stock.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.inventory.stock.domains.StockConfidenceLevel;

@ApplicationScoped
public class StockConfidenceLevelRepository implements PanacheRepository<StockConfidenceLevel> {
}