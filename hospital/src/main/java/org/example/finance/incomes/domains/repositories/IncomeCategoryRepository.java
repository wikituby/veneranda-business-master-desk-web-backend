package org.example.finance.incomes.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.finance.incomes.domains.IncomeCategory;

@ApplicationScoped
public class IncomeCategoryRepository implements PanacheRepository<IncomeCategory> {
}
