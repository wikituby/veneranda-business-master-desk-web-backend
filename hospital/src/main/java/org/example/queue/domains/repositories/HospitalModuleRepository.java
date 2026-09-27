package org.example.queue.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.queue.domains.HospitalModule;

@ApplicationScoped
public class HospitalModuleRepository implements PanacheRepository<HospitalModule> {
}
