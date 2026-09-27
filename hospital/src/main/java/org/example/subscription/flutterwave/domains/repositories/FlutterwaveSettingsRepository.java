package org.example.subscription.flutterwave.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.subscription.flutterwave.domains.FlutterwaveSettings;

@ApplicationScoped
public class FlutterwaveSettingsRepository implements PanacheRepository<FlutterwaveSettings> {
    public FlutterwaveSettings getSingleton() {
        return findAll().firstResult();
    }
}