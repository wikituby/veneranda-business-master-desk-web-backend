package org.example.cafeteria.sales.saleDone.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.cafeteria.sales.saleDone.domains.Sale;

@ApplicationScoped
public class SaleRepository implements PanacheRepository<Sale> {
}






