package org.example.subscription.dgateway.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.subscription.dgateway.domains.DGatewaySettings;

@ApplicationScoped
public class DGatewaySettingsRepository implements PanacheRepository<DGatewaySettings> {
    public DGatewaySettings getSingleton() {
        return findAll().firstResult();
    }
}