package org.example.lab.multipleParameterReport.cbc.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab.multipleParameterReport.cbc.domains.Cbc;

@ApplicationScoped
public class CbcRepository implements PanacheRepository<Cbc> {
}
