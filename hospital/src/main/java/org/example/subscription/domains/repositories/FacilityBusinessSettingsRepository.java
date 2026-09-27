package org.example.subscription.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.subscription.domains.FacilityBusinessSettings;

import java.util.Optional;

@ApplicationScoped
public class FacilityBusinessSettingsRepository implements PanacheRepository<FacilityBusinessSettings> {

    public Optional<FacilityBusinessSettings> findByFacilityId(Long facilityId) {
        if (facilityId == null) {
            return Optional.empty();
        }
        return find("facilityId", facilityId).firstResultOptional();
    }
}
