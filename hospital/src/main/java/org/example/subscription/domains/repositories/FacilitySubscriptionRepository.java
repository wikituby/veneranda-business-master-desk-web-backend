package org.example.subscription.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.subscription.domains.FacilitySubscription;

import java.util.Optional;

@ApplicationScoped
public class FacilitySubscriptionRepository implements PanacheRepository<FacilitySubscription> {

    public Optional<FacilitySubscription> findLatestByFacilityId(Long facilityId) {
        if (facilityId == null) {
            return Optional.empty();
        }
        return find("facilityId = ?1 order by id desc", facilityId).firstResultOptional();
    }

    public boolean wasActivationCodeUsed(String activationCodeKey) {
        if (activationCodeKey == null || activationCodeKey.isBlank()) {
            return false;
        }
        return count("activationTokenUsed", activationCodeKey.trim()) > 0;
    }
}
