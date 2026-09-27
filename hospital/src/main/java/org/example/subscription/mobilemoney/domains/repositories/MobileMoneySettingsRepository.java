package org.example.subscription.mobilemoney.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.subscription.mobilemoney.domains.MobileMoneySettings;

@ApplicationScoped
public class MobileMoneySettingsRepository implements PanacheRepository<MobileMoneySettings> {

    public MobileMoneySettings getSingleton() {
        return findAll().firstResult();
    }
}
