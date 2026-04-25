package org.example.lab.singleStatementReport.randomGlucose.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab.singleStatementReport.randomGlucose.domains.Rbs;

@ApplicationScoped
public class RbsRepository implements PanacheRepository<Rbs> {
}
