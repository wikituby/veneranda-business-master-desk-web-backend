package org.example.cafeteria.sales.saleDay.domains.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.cafeteria.sales.saleDay.domains.SaleDay;

@ApplicationScoped
public class SaleDayRepository implements PanacheRepository<SaleDay> {
}






