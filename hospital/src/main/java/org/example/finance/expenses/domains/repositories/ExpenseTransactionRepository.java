package org.example.finance.expenses.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.finance.expenses.domains.ExpenseTransaction;

@ApplicationScoped
public class ExpenseTransactionRepository implements PanacheRepository<ExpenseTransaction> {
}






