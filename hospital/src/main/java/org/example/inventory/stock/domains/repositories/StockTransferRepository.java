package org.example.inventory.stock.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.inventory.stock.domains.StockTransfer;

@ApplicationScoped
public class StockTransferRepository implements PanacheRepository<StockTransfer> {
}
