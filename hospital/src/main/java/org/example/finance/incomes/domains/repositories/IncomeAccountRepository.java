package org.example.finance.incomes.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.finance.incomes.domains.IncomeAccount;

@ApplicationScoped
public class IncomeAccountRepository implements PanacheRepository<IncomeAccount> {
}
