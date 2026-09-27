package org.example.backup.domains.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.backup.domains.BackupSettings;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class BackupSettingsRepository implements PanacheRepository<BackupSettings> {

    public BackupSettings getSingleton() {
        return findAll().firstResult();
    }
}
