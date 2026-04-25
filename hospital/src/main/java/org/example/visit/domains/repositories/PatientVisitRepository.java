package org.example.visit.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.visit.domains.PatientVisit;

@ApplicationScoped
public class PatientVisitRepository implements PanacheRepository<PatientVisit> {
}






