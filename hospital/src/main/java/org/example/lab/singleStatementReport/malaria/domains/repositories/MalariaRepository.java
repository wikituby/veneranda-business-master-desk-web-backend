package org.example.lab.singleStatementReport.malaria.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab.singleStatementReport.malaria.domains.Malaria;

@ApplicationScoped
public class MalariaRepository implements PanacheRepository<Malaria> {
}










