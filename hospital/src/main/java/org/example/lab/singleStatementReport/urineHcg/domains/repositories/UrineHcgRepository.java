package org.example.lab.singleStatementReport.urineHcg.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab.singleStatementReport.urineHcg.domains.UrineHcg;

@ApplicationScoped
public class UrineHcgRepository implements PanacheRepository<UrineHcg> {
}
