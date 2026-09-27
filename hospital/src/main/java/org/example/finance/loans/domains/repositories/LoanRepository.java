package org.example.finance.loans.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.finance.loans.domains.Loan;

@ApplicationScoped
public class LoanRepository implements PanacheRepository<Loan> {
}
