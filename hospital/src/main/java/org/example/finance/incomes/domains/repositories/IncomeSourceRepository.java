package org.example.finance.incomes.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.finance.incomes.domains.IncomeSource;

@ApplicationScoped
public class IncomeSourceRepository implements PanacheRepository<IncomeSource> {
}
