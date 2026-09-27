package org.example.finance.incomes.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.finance.incomes.domains.IncomeEntry;

@ApplicationScoped
public class IncomeEntryRepository implements PanacheRepository<IncomeEntry> {
}