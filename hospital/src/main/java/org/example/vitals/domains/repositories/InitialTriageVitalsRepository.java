package org.example.vitals.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.vitals.domains.InitialTriageVitals;

@ApplicationScoped
public class InitialTriageVitalsRepository implements PanacheRepository<InitialTriageVitals> {
}






