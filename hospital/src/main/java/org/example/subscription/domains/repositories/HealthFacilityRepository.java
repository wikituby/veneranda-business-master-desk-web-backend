package org.example.subscription.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.subscription.domains.HealthFacility;

@ApplicationScoped
public class HealthFacilityRepository implements PanacheRepository<HealthFacility> {
}
