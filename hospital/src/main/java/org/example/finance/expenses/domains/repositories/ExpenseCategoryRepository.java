package org.example.finance.expenses.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.finance.expenses.domains.ExpenseCategory;

@ApplicationScoped
public class ExpenseCategoryRepository implements PanacheRepository<ExpenseCategory> {
}






