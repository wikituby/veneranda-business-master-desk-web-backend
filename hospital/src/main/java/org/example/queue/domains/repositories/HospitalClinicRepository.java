package org.example.queue.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.queue.domains.HospitalClinic;

@ApplicationScoped
public class HospitalClinicRepository implements PanacheRepository<HospitalClinic> {
}
