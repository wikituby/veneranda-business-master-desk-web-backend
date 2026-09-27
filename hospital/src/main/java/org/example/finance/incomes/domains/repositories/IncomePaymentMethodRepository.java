package org.example.finance.incomes.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.finance.incomes.domains.IncomePaymentMethod;

@ApplicationScoped
public class IncomePaymentMethodRepository implements PanacheRepository<IncomePaymentMethod> {
}
