package org.example.lab.singleStatementReport.hiv.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab.singleStatementReport.hiv.domains.Hiv;

@ApplicationScoped
public class HivRepository implements PanacheRepository<Hiv> {
}
