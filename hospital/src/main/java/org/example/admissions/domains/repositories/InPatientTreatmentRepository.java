package org.example.admissions.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.admissions.domains.InPatientTreatment;

@ApplicationScoped
public class InPatientTreatmentRepository implements PanacheRepository<InPatientTreatment> {
}






