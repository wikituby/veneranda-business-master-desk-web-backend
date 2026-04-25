package org.example.treatment.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.treatment.domains.TreatmentRequested;

@ApplicationScoped
public class TreatmentRequestedRepository implements PanacheRepository<TreatmentRequested> {
}




